package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.client.LinkClient;
import backend.academy.bot.command.ListCommand;
import backend.academy.bot.dto.LinkResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListCommandTest {
    private ListCommand listCommand;
    private LinkClient linkClient;
    private Update update;
    private Message message;
    private Chat chat;

    @BeforeEach
    void setUp() {
        linkClient = mock(LinkClient.class);
        listCommand = new ListCommand(linkClient);

        update = mock(Update.class);
        message = mock(Message.class);
        chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
    }

    @Test
    void shouldReturnNoLinksMessage_WhenNoLinksTracked() {
        // Arrange
        when(linkClient.getLinks(123L)).thenReturn(List.of());

        // Act
        String result = listCommand.handle(update);

        // Assert
        assertEquals("📭 No links are currently being tracked.", result);
    }

    @Test
    void shouldReturnFormattedLinks_WhenLinksAreTracked() {
        // Arrange
        List<LinkResponse> links = List.of(
                new LinkResponse(1L, "https://github.com/user/repo", List.of("tag1"), List.of("filter1")),
                new LinkResponse(2L, "https://stackoverflow.com/q/12345", List.of(), List.of("new")));

        when(linkClient.getLinks(123L)).thenReturn(links);

        // Act
        String result = listCommand.handle(update);

        // Assert
        String expected =
                """
                📌 *Tracked Links:*
                - https://github.com/user/repo
                  🏷 *Tags:* tag1
                  🔍 *Filters:* filter1
                - https://stackoverflow.com/q/12345
                  🔍 *Filters:* new
                """;
        assertEquals(expected.trim(), result.trim());
    }
}
