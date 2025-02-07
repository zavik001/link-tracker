package backend.academy.bot.controller;

import backend.academy.bot.service.BotService;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CommandHandler {

    private final BotService botService;

    public String processUpdate(Update update) {
        return botService.handleCommand(update);
    }
}
