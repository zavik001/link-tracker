package backend.academy.bot.client;

import backend.academy.bot.config.AppProperties;
import backend.academy.bot.dto.UntrackLinkRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class TagClient {
    private final RestClient restClient = RestClient.create();
    private final ErrorHandler errorHandler;
    private final String apiUrl;

    public TagClient(AppProperties appProperties, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.apiUrl = appProperties.scrapperApiUrl() + "/tags";
    }

    public List<String> getTags(Long chatId) {
        try {
            return restClient
                    .get()
                    .uri(apiUrl)
                    .header("Tg-Chat-Id", chatId.toString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<String>>() {});
        } catch (RestClientResponseException e) {
            log.error(
                    "❌ Error getting tags for chat {}: {} - {}",
                    chatId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return List.of(errorHandler.extractErrorMessage(e));
        }
    }

    public List<String> getLinksByTag(Long chatId, String tag) {
        try {
            return restClient
                    .post()
                    .uri(apiUrl)
                    .header("Tg-Chat-Id", chatId.toString())
                    .body(new UntrackLinkRequest(tag))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<String>>() {});
        } catch (RestClientResponseException e) {
            log.error(
                    "❌ Error getting links by tag {} for chat {}: {} - {}",
                    tag,
                    chatId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return List.of();
        }
    }
}
