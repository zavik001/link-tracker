package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class StopCommand implements Command {
    private final ScrapperClient scrapperClient;

    public StopCommand(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

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
        return scrapperClient.deleteChat(chatId);
    }
}
