package backend.academy.scrapper.client.update.client;

import backend.academy.scrapper.client.ErrorHandler;
import backend.academy.scrapper.config.AppProperties;
import backend.academy.scrapper.dto.UpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@ConditionalOnProperty(name = "transport.type", havingValue = "http")
public class HttpUpdateClient implements UpdateClient {

    private final RestClient restClient = RestClient.create();
    private final ErrorHandler errorHandler;
    private final String apiUrl;

    public HttpUpdateClient(AppProperties appProperties, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.apiUrl = appProperties.botApiUrl() + "/updates";
    }

    @Override
    public void sendUpdate(UpdateResponse updateResponse) {
        try {
            restClient.post().uri(apiUrl).body(updateResponse).retrieve().body(UpdateResponse.class);
        } catch (RestClientResponseException e) {
            log.error("❌ Error sending update (HTTP): {}", errorHandler.extractErrorMessage(e));
        } catch (Exception e) {
            log.error("❌ Error sending update (HTTP): {}", e.getMessage());
        }
    }
}
