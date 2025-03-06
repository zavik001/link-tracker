package backend.academy.scrapper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(@NotNull Long chatId, @NotBlank String username) {}
