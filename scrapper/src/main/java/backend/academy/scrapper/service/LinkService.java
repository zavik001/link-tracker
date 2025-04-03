package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.exception.LinkAlredyExistsException;
import backend.academy.scrapper.repository.DbRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class LinkService {
    private final DbRepository dbRepository;

    public ListLinksResponse getAllLinks(Long chatId) {
        if (!dbRepository.existById(chatId)) {
            throw new ChatNotFoundException("Chat not found");
        }

        List<LinkResponse> links = dbRepository.getAllLinksById(chatId);
        return new ListLinksResponse(links, links.size());
    }

    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        if (!dbRepository.existById(chatId)) {
            throw new ChatNotFoundException("Chat not found");
        }

        if (dbRepository.existByLink(chatId, request.link())) {
            throw new LinkAlredyExistsException("Link already tracked");
        }

        dbRepository.saveByLink(chatId, request);

        return new LinkResponse(chatId, request.link(), request.tags(), request.filters());
    }

    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        if (!dbRepository.existById(chatId)) {
            throw new ChatNotFoundException("Chat not found");
        }

        return dbRepository.deleteByLink(chatId, request.link());
    }
}
