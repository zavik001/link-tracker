package backend.academy.bot.service;

import backend.academy.bot.command.Command;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class BotService {

    private final List<Command> commands;

    public BotService(List<Command> commands) {
        this.commands = commands;
    }

    public String handleUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return "Message not recognized.";
        }

        String commandText = update.message().text().split(" ")[0];

        return commands.stream()
                .filter(cmd -> cmd.command().equalsIgnoreCase(commandText))
                .findFirst()
                .map(cmd -> cmd.handle(update))
                .orElse("Unknown command. Use /help for a list of commands.");
    }
}
