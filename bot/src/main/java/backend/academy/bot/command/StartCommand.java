package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {

    private final ScrapperClient scrapperClient;

    public StartCommand(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "Register in the bot";
    }

    @Override
    public String handle(Update update) {
        Long chatId = update.message().chat().id();
        String username = update.message().from().username();
        return scrapperClient.registerUser(chatId, username);
    }
}
