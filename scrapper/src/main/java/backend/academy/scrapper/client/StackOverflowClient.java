package backend.academy.scrapper.client;

import backend.academy.scrapper.config.ScrapperConfig;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class StackOverflowClient {
    private final RestClient restClient;
    private final String apiKey;
    private final String accessToken;

    private static final Pattern STACK_OVERFLOW_PATTERN =
            Pattern.compile("https://stackoverflow\\.com/(?<type>questions|users|tags)/(?<id>\\d+)(/.*)?");

    private static final Pattern STACK_EXCHANGE_PATTERN = Pattern.compile(
            "https://(?<site>[^/]+)\\.stackexchange\\.com/(?<type>questions|users|tags)/(?<id>\\d+)(/.*)?");

    public StackOverflowClient(ScrapperConfig scrapperConfig) {
        this.apiKey = scrapperConfig.stackOverflow().key();
        this.accessToken = scrapperConfig.stackOverflow().accessToken();
        this.restClient = RestClient.builder()
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    public Map<String, Object> getLastUpdated(String url) {
        String apiUrl = convertToApiUrl(url);
        if (apiUrl == null) {
            return Map.of();
        }

        try {
            Map<String, Object> response =
                    restClient.get().uri(apiUrl).retrieve().body(new ParameterizedTypeReference<>() {});

            if (response == null || !response.containsKey("items")) {
                log.warn("⚠️ Empty or invalid response from Stack Overflow API for {}", apiUrl);
                return Map.of();
            }

            // log.info("📩 Response from Stack Overflow API: {}", response);
            return response;

        } catch (RestClientResponseException e) {
            log.error("❌ Stack Overflow API error: Status {} - {}", e.getStatusCode(), e.getStatusText());
        } catch (ResourceAccessException e) {
            log.error("❌ Stack Overflow API connection error: {}", e.getMessage());
        }

        return Map.of();
    }

    private String convertToApiUrl(String url) {
        Matcher matcher = STACK_OVERFLOW_PATTERN.matcher(url);
        if (matcher.matches()) {
            return String.format(
                    "https://api.stackexchange.com/2.3/%s/%s?site=stackoverflow&key=%s",
                    matcher.group("type"), matcher.group("id"), apiKey);
        }

        matcher = STACK_EXCHANGE_PATTERN.matcher(url);
        if (matcher.matches()) {
            return String.format(
                    "https://api.stackexchange.com/2.3/%s/%s?site=%s&key=%s",
                    matcher.group("type"), matcher.group("id"), matcher.group("site"), apiKey);
        }

        log.warn("⚠️ URL is not a Stack Overflow/Stack Exchange link: {}", url);
        return null;
    }
}
