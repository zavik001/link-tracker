package backend.academy.bot.service;

import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;

@Service
public class BotService {

    public String handleCommand(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return "Unknown command";
        }

        String command = update.message().text().split(" ")[0];

        return switch (command) {
            case "/start" -> handleStart();
            case "/help" -> handleHelp();
            default -> "Unknown command. Use /help to see available commands.";
        };
    }

    private String handleStart() {
        return "Hello! I'm your bot. Use /help to see the list of commands.";
    }

    private String handleHelp() {
        return "Available commands:\n" + "/start - Start the bot\n" + "/help - Show help";
    }
}
