package backend.academy.bot.client;

import backend.academy.bot.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class ChatClient {
    private final RestClient restClient = RestClient.create();
    private final ErrorHandler errorHandler;
    private final String apiUrl;

    public ChatClient(AppProperties appProperties, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.apiUrl = appProperties.scrapperApiUrl() + "/tg-chat";
    }

    public String registerChat(Long chatId) {
        try {
            return restClient.post().uri(apiUrl + "/" + chatId).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            log.error("❌ Chat registration error {}: {} - {}", chatId, e.getStatusCode(), e.getResponseBodyAsString());
            return errorHandler.extractErrorMessage(e);
        }
    }

    public String deleteChat(Long chatId) {
        try {
            return restClient.delete().uri(apiUrl + "/" + chatId).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            log.error("❌ Chat deletion error {}: {} - {}", chatId, e.getStatusCode(), e.getResponseBodyAsString());
            return errorHandler.extractErrorMessage(e);
        }
    }
}
