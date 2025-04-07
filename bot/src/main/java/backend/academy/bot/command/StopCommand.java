package backend.academy.bot.command;

import backend.academy.bot.client.ChatClient;
import backend.academy.bot.service.ListStringCacheService;
import backend.academy.bot.service.UpdateCacheServis;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StopCommand implements Command {
    private final ChatClient scrapperClient;
    private final ListStringCacheService listStringCacheService;
    private final UpdateCacheServis updateCacheServis;

    @Override
    public String command() {
        return "/stop";
    }

    @Override
    public String description() {
        return "Delete chat and stop working with the bot";
    }

    @Override
    public String handle(Update update) {
        Long chatId = update.message().chat().id();
        updateCacheServis.deleteLinks(chatId);
        List<String> tags = listStringCacheService.getTags(chatId);
        listStringCacheService.deleteTags(chatId);
        if (tags != null) {
            for (String tag : tags) {
                listStringCacheService.deleteTagLinks(chatId, tag);
            }
        }
        return scrapperClient.deleteChat(chatId);
    }
}
