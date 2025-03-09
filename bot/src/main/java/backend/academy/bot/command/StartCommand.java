package backend.academy.bot.command;

import backend.academy.bot.client.ChatClient;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {

    private final ChatClient scrapperClient;

    public StartCommand(ChatClient scrapperClient) {
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
        return scrapperClient.registerChat(chatId);
    }
}
