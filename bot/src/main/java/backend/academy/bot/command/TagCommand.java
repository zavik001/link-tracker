package backend.academy.bot.command;

import backend.academy.bot.client.TagClient;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TagCommand implements Command {
    private final TagClient scrapperClient;

    @Override
    public String command() {
        return "/tag";
    }

    @Override
    public String description() {
        return "Get tags (/tag) or find links by tag (/tag <tag>)";
    }

    @Override
    public String handle(Update update) {
        long chatId = update.message().chat().id();
        String[] args = update.message().text().split(" ", 2);

        if (args.length == 1) {
            List<String> tags = scrapperClient.getTags(chatId);
            if (tags.isEmpty()) {
                return "📭 No tags found.";
            }
            return "🏷 *Tags:* " + String.join(", ", tags);
        }

        String tag = args[1].trim();
        List<String> links = scrapperClient.getLinksByTag(chatId, tag);

        if (links.isEmpty()) {
            return "❌ No links found for tag: " + tag;
        }

        return "🏷 *Links:*\n"
                + IntStream.range(0, links.size())
                        .mapToObj(i -> (i + 1) + ". 🔗 " + links.get(i))
                        .collect(Collectors.joining("\n"));
    }
}
