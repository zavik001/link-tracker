package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.exception.LinkAlredyExistsException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.UpdateRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LinkService {
    private final ChatRepository chatRepository;
    private final UpdateRepository updateRepository;

    public LinkService(ChatRepository chatRepository, UpdateRepository updateRepository) {
        this.chatRepository = chatRepository;
        this.updateRepository = updateRepository;
    }

    public ListLinksResponse getAllLinks(Long chatId) {
        if (!chatRepository.exists(chatId)) {
            throw new ChatNotFoundException("Chat not found");
        }

        List<LinkResponse> links = chatRepository.getAllLinks(chatId);
        return new ListLinksResponse(links, links.size());
    }

    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        ChatEntity chat =
                chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        if (chat.links().contains(request.link())) {
            throw new LinkAlredyExistsException("Link already tracked");
        }

        chatRepository.addLink(chatId, request);

        updateRepository.addLink(chatId, request.link());

        return new LinkResponse(chatId, request.link(), request.tags(), request.filters());
    }

    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        ChatEntity chat =
                chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        return chatRepository.removeLink(chat, request);
    }
}
