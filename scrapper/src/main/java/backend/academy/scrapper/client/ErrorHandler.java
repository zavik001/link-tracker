package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class ErrorHandler {

    private final ObjectMapper objectMapper;

    public ErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String extractErrorMessage(RestClientResponseException e) {
        try {
            ApiErrorResponse errorResponse =
                    objectMapper.readValue(e.getResponseBodyAsString(), ApiErrorResponse.class);

            if (errorResponse.exceptionMessage() != null) {
                return "❌ " + errorResponse.exceptionMessage();
            }
        } catch (Exception ex) {
            log.error("Error parsing JSON", ex);
        }

        return "❌ Server error. Please try again later.";
    }
}
