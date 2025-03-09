package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LinkService {
    private final ChatRepository chatRepository;

    public LinkService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public ListLinksResponse getAllLinks(Long chatId) {
        ChatEntity chat =
                chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        List<LinkResponse> links = chat.links().stream()
                .map(url -> new LinkResponse(
                        chatId,
                        url,
                        chat.tags().getOrDefault(url, List.of()),
                        chat.filters().getOrDefault(url, List.of())))
                .collect(Collectors.toList());

        return new ListLinksResponse(links, links.size());
    }

    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        ChatEntity chat =
                chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        chat.links().add(request.link());
        chat.tags().put(request.link(), request.tags());
        chat.filters().put(request.link(), request.filters());

        return new LinkResponse(chatId, request.link(), request.tags(), request.filters());
    }

    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        ChatEntity chat =
                chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        return chatRepository.removeLink(chat, request);
    }
}
