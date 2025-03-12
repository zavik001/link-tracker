package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.exception.LinkAlredyExistsException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.UpdateRepository;
import backend.academy.scrapper.service.LinkService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkServiceTest {
    private LinkService linkService;
    private ChatRepository chatRepository;
    private UpdateRepository updateRepository;
    private final Long chatId = 123L;
    private final String testUrl = "https://github.com/user/repo";
    private AddLinkRequest request;

    @BeforeEach
    void setUp() {
        chatRepository = mock(ChatRepository.class);
        updateRepository = mock(UpdateRepository.class);
        linkService = new LinkService(chatRepository, updateRepository);
        request = new AddLinkRequest(testUrl, List.of("tag1"), List.of("filter1"));
    }

    @Test
    void shouldAddLink_WhenChatExistsAndLinkIsNew() {
        // Arrange
        ChatEntity chatEntity = new ChatEntity(chatId);
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chatEntity));

        // Act
        LinkResponse response = linkService.addLink(chatId, request);

        // Assert
        assertEquals(chatId, response.id());
        assertEquals(testUrl, response.url());
        assertEquals(request.tags(), response.tags());
        assertEquals(request.filters(), response.filters());
        verify(chatRepository, times(1)).addLink(chatId, request);
        verify(updateRepository, times(1)).addLink(chatId, testUrl);
    }

    @Test
    void shouldThrowChatNotFoundException_WhenChatDoesNotExist() {
        // Arrange
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChatNotFoundException.class, () -> linkService.addLink(chatId, request));
        verify(chatRepository, never()).addLink(anyLong(), any());
        verify(updateRepository, never()).addLink(anyLong(), any());
    }

    @Test
    void shouldThrowLinkAlreadyExistsException_WhenLinkAlreadyTracked() {
        // Arrange
        ChatEntity chatEntity = new ChatEntity(chatId);
        chatEntity.links().add(testUrl);
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chatEntity));

        // Act & Assert
        assertThrows(LinkAlredyExistsException.class, () -> linkService.addLink(chatId, request));
        verify(chatRepository, never()).addLink(anyLong(), any());
        verify(updateRepository, never()).addLink(anyLong(), any());
    }

    @Test
    void shouldRemoveLinkSuccessfully_WhenChatAndLinkExist() {
        // Arrange
        ChatEntity chatEntity = new ChatEntity(chatId);
        chatEntity.links().add(testUrl);
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chatEntity));

        RemoveLinkRequest removeRequest = new RemoveLinkRequest(testUrl);

        // Act
        linkService.removeLink(chatId, removeRequest);

        // Assert
        verify(updateRepository, times(1)).removeLink(chatId, testUrl);
        verify(chatRepository, times(1)).removeLink(chatEntity, removeRequest);
    }
}
