package backend.academy.scrapper.client;

import backend.academy.scrapper.config.ScrapperConfig;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class GitHubClient {
    private final RestClient restClient;
    private final String githubToken;

    private static final Pattern GITHUB_PATTERN = Pattern.compile(
            "https://github\\.com/(?<owner>[^/]+)/(?<repo>[^/]+)(?:/(?<type>issues|pull|discussions|releases|actions|wiki|tags|branches)/(?<number>\\d+))?");

    public GitHubClient(ScrapperConfig scrapperConfig) {
        this.githubToken = scrapperConfig.githubToken();
        this.restClient = RestClient.builder()
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public Map<String, Object> getLastUpdated(String githubUrl) {
        Matcher matcher = GITHUB_PATTERN.matcher(githubUrl);

        if (!matcher.matches()) {
            log.warn("⚠️ URL is not a GitHub link: {}", githubUrl);
            return Map.of();
        }

        String owner = matcher.group("owner");
        String repo = matcher.group("repo");
        String type = matcher.group("type");
        String number = matcher.group("number");

        String apiUrl = (type == null)
                ? String.format("https://api.github.com/repos/%s/%s", owner, repo)
                : String.format("https://api.github.com/repos/%s/%s/issues/%s", owner, repo, number);

        try {
            Map<String, Object> response =
                    restClient.get().uri(apiUrl).retrieve().body(new ParameterizedTypeReference<>() {});

            if (response == null || response.isEmpty()) {
                log.warn("⚠️ Empty response from GitHub API for {}", apiUrl);
                return Map.of();
            }

            // log.info("📩 Response from GitHub API: {}", response);
            return response;

        } catch (RestClientResponseException e) {
            // log.error("❌ GitHub API error: Status {} - {}\nResponse Body: {}",
            // e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString());
            log.error("❌ GitHub API error: Status {} - {}", e.getStatusCode(), e.getStatusText());
            return Map.of();
        }
    }
}
