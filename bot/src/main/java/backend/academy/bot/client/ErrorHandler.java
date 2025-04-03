package backend.academy.bot.client;

import backend.academy.bot.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
@Slf4j
@Component
public class ErrorHandler {

    private final ObjectMapper objectMapper;

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
