package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.client.UpdateClient;
import backend.academy.scrapper.dto.UpdateResponse;
import backend.academy.scrapper.repository.UpdateRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UpdateService {
    private final UpdateRepository updateRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final UpdateClient updateClient;

    @Value("${scheduler.update-interval}")
    private long updateInterval;

    public UpdateService(
            UpdateRepository updateRepository,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient,
            UpdateClient updateClient) {
        this.updateRepository = updateRepository;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
        this.updateClient = updateClient;
    }

    public void checkUpdates() {
        Map<String, Instant> lastUpdates = getLastUpdates();
        log.info("Last updates: {}", lastUpdates.toString());

        lastUpdates.forEach((url, lastUpdated) -> {
            if (isRecentUpdate(lastUpdated)) {
                List<Long> chatIds = updateRepository.getChatIdsByUrl(url);
                if (!chatIds.isEmpty()) {
                    updateClient.sendUpdate(new UpdateResponse((long) url.hashCode(), url, "Updated", chatIds));
                }
            }
        });
    }

    private Map<String, Instant> getLastUpdates() {
        return updateRepository.getAllLinks().stream()
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
        return lastUpdated.isAfter(Instant.now().minusMillis(updateInterval));
    }
}
