package backend.academy.bot.service;

import backend.academy.bot.command.Command;
import backend.academy.bot.command.TrackCommand;
import com.pengrad.telegrambot.model.Update;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class BotService {

    private final List<Command> commands;
    private final Map<Long, Command> activeStates = new HashMap<>();

    public BotService(List<Command> commands) {
        this.commands = commands;
    }

    public String handleUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return "Message not recognized.";
        }

        long chatId = update.message().chat().id();
        String messageText = update.message().text();
        String commandText = messageText.split(" ")[0];

        if (activeStates.containsKey(chatId)) {
            Command activeCommand = activeStates.get(chatId);
            String response = activeCommand.handle(update);

            if (activeCommand instanceof TrackCommand trackCommand && trackCommand.isTrackingCompleted(chatId)) {
                activeStates.remove(chatId);
            }

            return response;
        }

        Command command = commands.stream()
                .filter(cmd -> cmd.command().equalsIgnoreCase(commandText))
                .findFirst()
                .orElse(null);

        if (command != null) {
            String response = command.handle(update);

            if (command instanceof TrackCommand trackCommand && !trackCommand.isTrackingCompleted(chatId)) {
                activeStates.put(chatId, command);
            }

            return response;
        }

        return "Unknown command. Use /help for a list of commands.";
    }
}
