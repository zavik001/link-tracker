package backend.academy.bot.command;

import backend.academy.bot.client.LinkClient;
import backend.academy.bot.dto.UntrackLinkRequest;
import backend.academy.bot.util.LinkValidator;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UntrackCommand implements Command {
    private final LinkClient scrapperClient;

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Stop tracking a link (GitHub/Stack Overflow only)";
    }

    @Override
    public String handle(Update update) {
        String message = update.message().text();
        String[] parts = message.split(" ");

        if (parts.length < 2) {
            return "❌ *Usage:* `/untrack <URL>`";
        }

        String url = parts[1];

        if (!LinkValidator.isValidLink(url)) {
            return "🚫 *Invalid link!* Only GitHub and Stack Overflow links are supported.";
        }

        long chatId = update.message().chat().id();

        return scrapperClient.removeLink(chatId, new UntrackLinkRequest(url));
    }
}
