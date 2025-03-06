package backend.academy.bot.command;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {

    private final List<Command> commands;

    public HelpCommand(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "Show list of commands";
    }

    @Override
    public String handle(com.pengrad.telegrambot.model.Update update) {
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
