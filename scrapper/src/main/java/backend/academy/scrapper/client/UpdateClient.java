package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.UpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class UpdateClient {
    private final RestClient restClient = RestClient.create();
    private final ErrorHandler errorHandler;
    private static final String API_URL = "http://localhost:8080/updates";

    public UpdateClient(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void sendUpdate(UpdateResponse updateResponse) {
        try {
            restClient.post().uri(API_URL).body(updateResponse).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("❌ Error sending update: {}", errorHandler.extractErrorMessage(e));
        }
    }
}
