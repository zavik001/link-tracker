package backend.academy.bot.controller;

import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.exception.ChatNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleChatNotFound(ChatNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    private List<String> getStackTrace(Exception e) {
        return Arrays.stream(e.getStackTrace())
                .limit(5)
                .map(StackTraceElement::toString)
                .toList();
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(Exception e, HttpStatus status) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Request error",
                String.valueOf(status.value()),
                e.getClass().getSimpleName(),
                e.getMessage(),
                getStackTrace(e));
        return ResponseEntity.status(status).body(errorResponse);
    }
}
