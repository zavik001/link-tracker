package backend.academy.bot.command;

import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class HelpCommand implements Command {

    private final List<Command> commands;

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "Show list of commands";
    }

    @Override
    public String handle(Update update) {
        StringBuilder helpText = new StringBuilder("Available commands:\n");
        for (Command cmd : commands) {
            helpText.append(cmd.command())
                    .append(" - ")
                    .append(cmd.description())
                    .append("\n");
        }
        return helpText.toString();
    }
}
