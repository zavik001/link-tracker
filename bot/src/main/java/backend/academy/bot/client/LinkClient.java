package backend.academy.bot.client;

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
    private static final String API_URL = "http://localhost:8081/links";

    public LinkClient(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public String addLink(Long chatId, TrackLinkRequest link) {
        try {
            return restClient
                    .post()
                    .uri(API_URL)
                    .header("Tg-Chat-Id", chatId.toString())
                    .body(link)
                    .retrieve()
                    .body(String.class);
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
                    .uri(API_URL)
                    .header("Tg-Chat-Id", chatId.toString())
                    .body(link)
                    .retrieve()
                    .body(LinkResponse.class);

            if (response != null && response.url() != null) {
                return "✅ Successfully stopped tracking: " + response.url();
            }

            return "❌ Link not found in chat";
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
                    .uri(API_URL)
                    .header("Tg-Chat-Id", chatId.toString())
                    .retrieve()
                    .body(ListLinksResponse.class);

            return (response != null && response.links() != null) ? response.links() : List.of();
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }
}
