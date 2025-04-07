package backend.academy.bot.command;

import backend.academy.bot.client.LinkClient;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.service.UpdateCacheServis;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ListCommand implements Command {
    private final LinkClient scrapperClient;
    private final UpdateCacheServis updateCacheServis;

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "List all tracked links";
    }

    @Override
    public String handle(Update update) {
        long chatId = update.message().chat().id();

        List<LinkResponse> links = updateCacheServis.getLinks(chatId);

        if (links == null) {
            links = scrapperClient.getLinks(chatId);
            updateCacheServis.setLinks(chatId, links);
        }

        if (links.isEmpty()) {
            return "📭 No links are currently being tracked.";
        }

        StringBuilder response = new StringBuilder("📌 *Tracked Links:*\n");
        for (LinkResponse link : links) {
            response.append("- ").append(link.url()).append("\n");

            if (!link.tags().isEmpty()) {
                response.append("  🏷 *Tags:* ")
                        .append(String.join(", ", link.tags()))
                        .append("\n");
            }
            if (!link.filters().isEmpty()) {
                response.append("  🔍 *Filters:* ")
                        .append(String.join(", ", link.filters()))
                        .append("\n");
            }
        }

        return response.toString();
    }
}
