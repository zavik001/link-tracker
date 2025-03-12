package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.service.BotService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BotServiceTest {
    private BotService botService;

    @BeforeEach
    void setUp() {
        botService = new BotService(List.of());
    }

    @Test
    void shouldReturnUnknownCommandMessage_whenCommandIsUnknown() {
        // Arrange
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/unknownCommand");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        // Act & Assert
        assertEquals("Unknown command. Use /help for a list of commands.", botService.handleUpdate(update));
    }
}
