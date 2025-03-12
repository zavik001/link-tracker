package backend.academy.bot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkUpdate(
        @NotNull(message = "ID can't be null") @Min(Long.MIN_VALUE) @Max(Long.MAX_VALUE) Long id,
        @NotBlank(message = "URL can't be empty") String url,
        @NotBlank(message = "description can't be empty") String update,
        @NotNull(message = "tgChatIds can't be null")
                List<@NotNull(message = "tgChatId can't be null") Long> chatIds) {}
