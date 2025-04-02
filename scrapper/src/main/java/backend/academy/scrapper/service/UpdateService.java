package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.client.UpdateClient;
import backend.academy.scrapper.dto.UpdateResponse;
import backend.academy.scrapper.repository.DbRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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

    @Value("${scheduler.batch-size}")
    private int batchSize;

    @Value("${scheduler.thread-count}")
    private int threadCount;

    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public void checkUpdates() {
        for (int offset = 0, batchSize = this.batchSize; ; offset += batchSize) {
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

        Map<String, Instant> lastUpdates = getLastUpdates(links);
        lastUpdates.forEach((url, lastUpdated) -> {
            if (isRecentUpdate(lastUpdated)) {
                List<Long> chatIds = dbRepository.getChatIdsByUrl(url);
                if (!chatIds.isEmpty()) {
                    updateClient.sendUpdate(new UpdateResponse((long) url.hashCode(), url, "Updated", chatIds));
                }
            }
        });
    }

    private Map<String, Instant> getLastUpdates(List<String> links) {
        return links.stream()
                .collect(Collectors.toMap(
                        url -> url,
                        url -> url.contains("github.com")
                                ? gitHubClient.getLastUpdated(url)
                                : stackOverflowClient.getLastUpdated(url)));
    }

    private boolean isRecentUpdate(Instant lastUpdated) {
        return !lastUpdated.equals(Instant.EPOCH)
                && lastUpdated.isAfter(Instant.now().minusSeconds(86400));
    }
}
