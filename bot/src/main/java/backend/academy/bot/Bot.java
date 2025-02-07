package backend.academy.bot;

import backend.academy.bot.config.BotConfig;
import backend.academy.bot.controller.CommandHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Bot {

    private final TelegramBot bot;
    private final CommandHandler commandHandler;

    public Bot(BotConfig botConfig, CommandHandler commandHandler) {
        this.bot = new TelegramBot(botConfig.telegramToken());
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void start() {
        bot.setUpdatesListener(this::processUpdates);
    }

    private int processUpdates(List<Update> updates) {
        for (Update update : updates) {
            String response = commandHandler.processUpdate(update);
            if (update.message() != null && update.message().chat() != null) {
                bot.execute(new com.pengrad.telegrambot.request.SendMessage(
                        update.message().chat().id(), response));
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
