package backend.academy.scrapper.client;

import backend.academy.scrapper.config.AppProperties;
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
    private final String apiUrl;

    public UpdateClient(AppProperties appProperties, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.apiUrl = appProperties.botApiUrl() + "/updates";
    }

    public void sendUpdate(UpdateResponse updateResponse) {
        try {
            restClient.post().uri(apiUrl).body(updateResponse).retrieve().body(UpdateResponse.class);
        } catch (RestClientResponseException e) {
            log.error("❌ Error sending update: {}", errorHandler.extractErrorMessage(e));
        }
    }
}
