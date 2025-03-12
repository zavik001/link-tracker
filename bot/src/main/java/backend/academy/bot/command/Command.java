package backend.academy.bot.command;

import com.pengrad.telegrambot.model.Update;

public interface Command {
    String command();

    String description();

    String handle(Update update);
}
