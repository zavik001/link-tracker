package backend.academy.bot;

import backend.academy.bot.service.BotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Bot {

    private final TelegramBot bot;
    private final BotService botService;

    public Bot(TelegramBot bot, BotService botService) {
        this.bot = bot;
        this.botService = botService;
    }

    @PostConstruct
    public void start() {
        registerCommands();
        bot.setUpdatesListener(this::processUpdates);
    }

    private void registerCommands() {
        List<BotCommand> botCommands = botService.commands().stream()
                .map(cmd -> new BotCommand(cmd.command(), cmd.description()))
                .collect(Collectors.toList());

        bot.execute(new SetMyCommands(botCommands.toArray(new BotCommand[0])));
    }

    private int processUpdates(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null && update.message().chat() != null) {
                Long chatId = update.message().chat().id();
                String response = botService.handleUpdate(update);
                bot.execute(new SendMessage(chatId, response));
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
