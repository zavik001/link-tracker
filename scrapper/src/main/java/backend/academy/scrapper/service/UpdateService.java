package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.client.UpdateClient;
import backend.academy.scrapper.dto.UpdateResponse;
import backend.academy.scrapper.repository.DbRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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

    @Value("${scheduler.update-interval}")
    private long updateInterval;

    public void checkUpdates() {
        Map<String, Instant> lastUpdates = getLastUpdates();
        // log.info("Last updates: {}", lastUpdates.toString());

        lastUpdates.forEach((url, lastUpdated) -> {
            if (isRecentUpdate(lastUpdated)) {
                List<Long> chatIds = dbRepository.getChatIdsByUrl(url);
                // log.info("Chat ids: {}", chatIds);
                if (!chatIds.isEmpty()) {
                    // log.info("Sending update for {} to {}", url, chatIds);
                    // UpdateResponse updateResponse = new UpdateResponse((long) url.hashCode(), url, "Updated",
                    // chatIds);
                    // log.info("Sending update: {}", updateResponse.toString());
                    updateClient.sendUpdate(new UpdateResponse((long) url.hashCode(), url, "Updated", chatIds));
                }
            }
        });
    }

    private Map<String, Instant> getLastUpdates() {
        return dbRepository.getAllLinks().stream()
                .collect(Collectors.toMap(
                        url -> url,
                        url -> url.contains("github.com")
                                ? gitHubClient.getLastUpdated(url)
                                : stackOverflowClient.getLastUpdated(url)));
    }

    private boolean isRecentUpdate(Instant lastUpdated) {
        if (lastUpdated.equals(Instant.EPOCH)) {
            return false;
        }
        // log.info("Last updated: {}", lastUpdated);
        return lastUpdated.isAfter(Instant.now().minusSeconds(updateInterval + 10000000));
    }
}
