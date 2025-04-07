package backend.academy.bot.command;

import backend.academy.bot.client.TagClient;
import backend.academy.bot.service.ListStringCacheService;
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
    private final ListStringCacheService listStringCacheService;

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
            List<String> tags = listStringCacheService.getTags(chatId);
            if (tags == null) {
                tags = scrapperClient.getTags(chatId);
                listStringCacheService.setTags(chatId, tags);
            }

            if (tags.isEmpty()) {
                return "📭 No tags found.";
            }
            return "🏷 *Tags:* " + String.join(", ", tags);
        }

        String tag = args[1].trim();
        List<String> links = listStringCacheService.getTagLinks(chatId, tag);
        if (links == null) {
            links = scrapperClient.getLinksByTag(chatId, tag);
            listStringCacheService.setTagLinks(chatId, tag, links);
        }

        if (links.isEmpty()) {
            return "❌ No links found for tag: " + tag;
        }

        final List<String> finalLinks = links;
        return "🏷 *Links:*\n"
                + IntStream.range(0, finalLinks.size())
                        .mapToObj(i -> (i + 1) + ". 🔗 " + finalLinks.get(i))
                        .collect(Collectors.joining("\n"));
    }
}
