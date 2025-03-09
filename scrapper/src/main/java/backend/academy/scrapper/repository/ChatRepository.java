package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.exception.LinkNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepository {
    private final Map<Long, ChatEntity> chats = new HashMap<>();

    public void save(ChatEntity chat) {
        chats.put(chat.id(), chat);
    }

    public Optional<ChatEntity> findById(Long chatId) {
        return Optional.ofNullable(chats.get(chatId));
    }

    public void delete(Long chatId) {
        chats.remove(chatId);
    }

    public boolean exists(Long chatId) {
        return chats.containsKey(chatId);
    }

    public List<LinkResponse> getAllLinks(Long chatId) {
        return findById(chatId)
                .map(chat -> chat.links().stream()
                        .map(link -> new LinkResponse(
                                (long) link.hashCode(),
                                link,
                                chat.tags().getOrDefault(link, Collections.emptyList()),
                                chat.filters().getOrDefault(link, Collections.emptyList())))
                        .toList())
                .orElse(Collections.emptyList());
    }

    public void addLink(Long chatId, AddLinkRequest request) {
        ChatEntity chat = chats.computeIfAbsent(chatId, ChatEntity::new);
        chat.links().add(request.link());
        chat.tags().put(request.link(), request.tags());
        chat.filters().put(request.link(), request.filters());
    }

    public LinkResponse removeLink(ChatEntity chat, RemoveLinkRequest request) {
        if (!chat.links().remove(request.link())) {
            throw new LinkNotFoundException("Link not found in chat");
        }

        List<String> removedTags = chat.tags().remove(request.link());
        List<String> removedFilters = chat.filters().remove(request.link());

        return new LinkResponse(
                chat.id(),
                request.link(),
                removedTags != null ? removedTags : List.of(),
                removedFilters != null ? removedFilters : List.of());
    }
}
