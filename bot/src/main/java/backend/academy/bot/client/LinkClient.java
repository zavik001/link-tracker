package backend.academy.bot.client;

import backend.academy.bot.config.AppProperties;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.TrackLinkRequest;
import backend.academy.bot.dto.UntrackLinkRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class LinkClient {
    private final RestClient restClient = RestClient.create();
    private final ErrorHandler errorHandler;
    private final String apiUrl;

    public LinkClient(AppProperties appProperties, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.apiUrl = appProperties.scrapperApiUrl() + "/links";
    }

    public String addLink(Long chatId, TrackLinkRequest link) {
        try {
            LinkResponse response = restClient
                    .post()
                    .uri(apiUrl)
                    .header("Tg-Chat-Id", chatId.toString())
                    .body(link)
                    .retrieve()
                    .body(LinkResponse.class);

            if (response != null && response.url() != null) {
                return "✅ Successfully added to tracking: " + response.url();
            }

            return "❌ Error adding link to be tracked " + link.link();
        } catch (RestClientResponseException e) {
            log.error(
                    "❌ Error adding link {} for chat {}: {} - {}",
                    link.link(),
                    chatId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return errorHandler.extractErrorMessage(e);
        }
    }

    public String removeLink(Long chatId, UntrackLinkRequest link) {
        try {
            LinkResponse response = restClient
                    .method(HttpMethod.DELETE)
                    .uri(apiUrl)
                    .header("Tg-Chat-Id", chatId.toString())
                    .body(link)
                    .retrieve()
                    .body(LinkResponse.class);

            if (response != null && response.url() != null) {
                return "✅ Successfully stopped tracking: " + response.url();
            }

            return "❌ Link not found";
        } catch (RestClientResponseException e) {
            log.error(
                    "❌ Error removing link {} for chat {}: {} - {}",
                    link,
                    chatId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return errorHandler.extractErrorMessage(e);
        }
    }

    public List<LinkResponse> getLinks(Long chatId) {
        try {
            ListLinksResponse response = restClient
                    .get()
                    .uri(apiUrl)
                    .header("Tg-Chat-Id", chatId.toString())
                    .retrieve()
                    .body(ListLinksResponse.class);

            return (response != null && response.links() != null) ? response.links() : List.of();
        } catch (RestClientResponseException e) {
            log.error(
                    "❌ Error getting links for chat {}: {} - {}",
                    chatId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            log.error("❌ Error - {}", errorHandler.extractErrorMessage(e));
            return List.of();
        }
    }
}
