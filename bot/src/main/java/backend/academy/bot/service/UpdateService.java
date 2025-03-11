package backend.academy.bot.service;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.exception.ChatNotFoundException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UpdateService {
    private final TelegramBot bot;

    public UpdateService(TelegramBot bot) {
        this.bot = bot;
    }

    public void sendUpdateToChats(LinkUpdate update) {
        if (update.tgChatIds().isEmpty()) {
            throw new ChatNotFoundException("Chat not found");
        }

        String message = "🔔 *New update!*\n\n" + "📌 *link:* "
                + update.url() + "\n" + "📝 *description:* "
                + update.description();

        for (Long chatId : update.tgChatIds()) {
            bot.execute(new SendMessage(chatId, message));
        }
    }
}
