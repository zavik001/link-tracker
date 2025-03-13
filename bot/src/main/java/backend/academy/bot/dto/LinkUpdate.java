package backend.academy.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkUpdate(
        @NotNull(message = "ID can't be null") Long id,
        @NotBlank(message = "URL can't be empty") String url,
        @NotBlank(message = "description can't be empty") String update,
        @NotNull(message = "tgChatIds can't be null")
                List<@NotNull(message = "tgChatId can't be null") Long> chatIds) {}
