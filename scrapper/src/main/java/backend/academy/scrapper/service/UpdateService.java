package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.client.update.client.UpdateClient;
import backend.academy.scrapper.dto.UpdateResponse;
import backend.academy.scrapper.repository.DbRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class UpdateService {
    private final DbRepository dbRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final UpdateClient updateClient;
    private ExecutorService executorService;
    private ObjectMapper objectMapper;
    private static final int MAX_LENGTH = 200;
    private static final int MAX_TITLE_LENGTH = 50;

    @Value("${scheduler.batch-size}")
    private int batchSize;

    @Value("${scheduler.thread-count}")
    private int threadCount;

    @Value("${scheduler.update-interval}")
    private int updateInterval;

    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(threadCount);
        this.objectMapper = new ObjectMapper();
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public void checkUpdates() {
        for (int offset = 0; ; offset += batchSize) {
            List<String> linksBatch = dbRepository.getLinksBatch(offset, batchSize);
            if (linksBatch.isEmpty()) break;

            log.info("Processing batch of {} links", linksBatch.size());
            processLinksBatchAsync(linksBatch);
        }
    }

    private void processLinksBatchAsync(List<String> linksBatch) {
        int totalLinks = linksBatch.size();
        int threadsToUse = Math.min(threadCount, totalLinks);
        int batchPartSize = (int) Math.ceil((double) totalLinks / threadsToUse);

        CompletableFuture<?>[] futures = new CompletableFuture[threadsToUse];

        for (int i = 0; i < threadsToUse; i++) {
            int start = i * batchPartSize;
            int end = Math.min(start + batchPartSize, totalLinks);
            List<String> subBatch = linksBatch.subList(start, end);

            futures[i] = CompletableFuture.runAsync(() -> processBatch(subBatch), executorService)
                    .exceptionally(e -> {
                        log.error("Error processing sub-batch: ", e);
                        return null;
                    });
        }

        CompletableFuture.allOf(futures).join();
        log.info("Finished processing batch of {} links", totalLinks);
    }

    private void processBatch(List<String> links) {
        log.info("Thread {} processing {} links", Thread.currentThread().getName(), links.size());

        Map<String, Object> updates = getUpdates(links);
        updates.forEach((url, updateData) -> {
            JsonNode json = parseJson(updateData);
            if (json == null) {
                return;
            }
            List<Long> chatIds = dbRepository.getChatIdsByUrl(url);
            if (chatIds.isEmpty()) return;

            for (Long chatId : chatIds) {
                List<String> filters = dbRepository.getFiltersByChatIdAndLink(chatId, url);
                log.info(
                        "Processing {} for chatId {} with filters {} in Thread {}",
                        url,
                        chatId,
                        filters,
                        Thread.currentThread().getName());

                String latestUpdate = extractRelevantUpdate(json, filters);
                if (latestUpdate != null) {
                    updateClient.sendUpdate(
                            new UpdateResponse((long) url.hashCode(), url, latestUpdate, List.of(chatId)));
                }
            }
        });
    }

    private Map<String, Object> getUpdates(List<String> links) {
        return links.stream()
                .collect(Collectors.toMap(
                        url -> url,
                        url -> url.contains("github.com")
                                ? gitHubClient.getLastUpdated(url)
                                : stackOverflowClient.getLastUpdated(url)));
    }

    public String extractRelevantUpdate(JsonNode json, List<String> filters) {
        Instant lastUpdated = extractLastUpdated(json);
        if (lastUpdated != null && !isRecentUpdate(lastUpdated)) {
            return null;
        }

        for (String filter : filters) {
            if (filter.startsWith("user=")) {
                String blockedUser = filter.substring("user=".length());
                if (matchesAntiUserFilter(json, blockedUser)) {
                    log.info("Blocked update from user={} by anti-filter", blockedUser);
                    return null;
                }
            }
        }

        String result = filters.stream()
                .filter(filter -> !filter.startsWith("user="))
                .map(filter -> {
                    List<String> values = findAllKeysInJson(json, filter);
                    String truncatedValues = values.stream()
                            .map(value -> value.length() > MAX_TITLE_LENGTH
                                    ? value.substring(0, MAX_TITLE_LENGTH) + "..."
                                    : value)
                            .collect(Collectors.joining(", "));
                    return values.isEmpty() ? null : filter + ": " + truncatedValues;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));

        return result.length() > MAX_LENGTH ? result.substring(0, MAX_LENGTH) + "..." : result;
    }

    private boolean matchesAntiUserFilter(JsonNode json, String blockedUser) {
        List<String> userKeys = List.of("author", "login", "user", "account_name", "owner");

        for (String key : userKeys) {
            List<String> values = findAllKeysInJson(json, key);
            for (String value : values) {
                if (value.equalsIgnoreCase(blockedUser)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JsonNode parseJson(Object updateData) {
        try {
            if (updateData instanceof String jsonString) {
                return objectMapper.readTree(jsonString);
            } else if (updateData instanceof Map) {
                return objectMapper.valueToTree(updateData);
            }
            log.warn("Unexpected updateData type: {}", updateData.getClass());
            return null;
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON: {}", updateData);
            return null;
        }
    }

    private Instant extractLastUpdated(JsonNode json) {
        if (json.has("last_activity_date")) {
            return Instant.ofEpochSecond(json.get("last_activity_date").asLong());
        }
        if (json.has("creation_date")) {
            return Instant.ofEpochSecond(json.get("creation_date").asLong());
        }

        List<String> dateFields = List.of("pushed_at", "updated_at", "created_at");
        for (String field : dateFields) {
            if (json.has(field)) {
                try {
                    return Instant.parse(json.get(field).asText());
                } catch (DateTimeParseException e) {
                    log.warn("Invalid date format in {}: {}", field, json.get(field));
                }
            }
        }

        return null;
    }

    private List<String> findAllKeysInJson(JsonNode json, String key) {
        List<String> results = new ArrayList<>();
        collectKeys(json, key, results);
        return results;
    }

    private void collectKeys(JsonNode json, String key, List<String> results) {
        if (json.isObject()) {
            json.fields().forEachRemaining(entry -> {
                if (entry.getKey().equals(key)) {
                    results.add(entry.getValue().asText());
                }
                collectKeys(entry.getValue(), key, results);
            });
        } else if (json.isArray()) {
            for (JsonNode node : json) {
                collectKeys(node, key, results);
            }
        }
    }

    private boolean isRecentUpdate(Instant lastUpdated) {
        return lastUpdated.isAfter(Instant.now().minusSeconds(updateInterval + 1000000));
    }
}
