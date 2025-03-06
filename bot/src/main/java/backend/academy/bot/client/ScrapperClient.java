package backend.academy.bot.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ScrapperClient {
    private final RestClient restClient = RestClient.create();
    private static final String API_URL = "http://localhost:8081/tg-chat";

    public String registerUser(Long chatId, String username) {
        UserRequest requestBody = new UserRequest(chatId, username);

        try {
            return restClient.post().uri(API_URL).body(requestBody).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            return extractErrorMessage(e);
        }
    }

    public String deleteChat(Long chatId) {
        String url = API_URL + "/" + chatId;

        try {
            return restClient.delete().uri(url).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            return extractErrorMessage(e);
        }
    }

    private String extractErrorMessage(RestClientResponseException e) {
        try {
            return e.getResponseBodyAs(String.class);
        } catch (Exception ex) {
            return "An error occurred: " + e.getStatusCode();
        }
    }

    public record UserRequest(Long chatId, String username) {}
}
