package backend.academy.bot.service;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.exception.ChatNotFoundException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UpdateService {
    private final TelegramBot bot;

    public void sendUpdateToChats(LinkUpdate update) {
        if (update.chatIds().isEmpty()) {
            throw new ChatNotFoundException("Chat not found");
        }

        String message =
                "🔔 *New update!*\n\n" + "📌 *link:* " + update.url() + "\n" + "📝 *description:* " + update.update();

        for (Long chatId : update.chatIds()) {
            bot.execute(new SendMessage(chatId, message));
        }
    }
}
