package backend.academy.bot.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class ChatClient {
    private final RestClient restClient = RestClient.create();
    private final ErrorHandler errorHandler;
    private static final String API_URL = "http://localhost:8081/tg-chat";

    public ChatClient(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public String registerChat(Long chatId) {
        try {
            return restClient.post().uri(API_URL + "/" + chatId).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            log.error("❌ Chat registration error {}: {} - {}", chatId, e.getStatusCode(), e.getResponseBodyAsString());
            return errorHandler.extractErrorMessage(e);
        }
    }

    public String deleteChat(Long chatId) {
        try {
            return restClient.delete().uri(API_URL + "/" + chatId).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            log.error("❌ Chat deletion error {}: {} - {}", chatId, e.getStatusCode(), e.getResponseBodyAsString());
            return errorHandler.extractErrorMessage(e);
        }
    }
}
