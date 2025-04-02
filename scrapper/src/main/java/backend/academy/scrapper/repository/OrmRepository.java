package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.ChatLinkEntity;
import backend.academy.scrapper.entity.ChatLinkFilterEntity;
import backend.academy.scrapper.entity.ChatLinkFilterId;
import backend.academy.scrapper.entity.ChatLinkId;
import backend.academy.scrapper.entity.ChatLinkTagEntity;
import backend.academy.scrapper.entity.ChatLinkTagId;
import backend.academy.scrapper.entity.FilterEntity;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.entity.TagEntity;
import backend.academy.scrapper.exception.FilterNotFoundException;
import backend.academy.scrapper.exception.TagNotFoundException;
import backend.academy.scrapper.repository.jpa.ChatLinkFilterRepository;
import backend.academy.scrapper.repository.jpa.ChatLinkRepository;
import backend.academy.scrapper.repository.jpa.ChatLinkTagRepository;
import backend.academy.scrapper.repository.jpa.ChatRepository;
import backend.academy.scrapper.repository.jpa.FilterRepository;
import backend.academy.scrapper.repository.jpa.LinkRepository;
import backend.academy.scrapper.repository.jpa.TagRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Repository
@ConditionalOnProperty(name = "database.access-type", havingValue = "ORM")
public class OrmRepository implements DbRepository {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final ChatLinkRepository chatLinkRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;
    private final ChatLinkTagRepository chatLinkTagRepository;
    private final ChatLinkFilterRepository chatLinkFilterRepository;

    @Override
    @Transactional
    public boolean existById(Long chatId) {
        return chatRepository.existsById(chatId);
    }

    @Override
    @Transactional
    public void saveById(Long chatId) {
        chatRepository.save(new ChatEntity(chatId, null));
    }

    @Override
    @Transactional
    public void deleteById(Long chatId) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        List<ChatLinkEntity> chatLinks = chatLinkRepository.findAllByChat(chat);

        for (ChatLinkEntity chatLink : chatLinks) {
            deleteByLink(chatId, chatLink.link().url());
        }

        chatRepository.delete(chat);
    }

    @Override
    public List<LinkResponse> getAllLinksById(Long chatId) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        return chatLinkRepository.findAllByChat(chat).stream()
                .map(cl -> new LinkResponse(
                        cl.link().id(),
                        cl.link().url(),
                        chatLinkTagRepository.findAllByChatAndLink(cl.chat(), cl.link()).stream()
                                .map(lt -> lt.tag().name())
                                .toList(),
                        chatLinkFilterRepository.findAllByChatAndLink(cl.chat(), cl.link()).stream()
                                .map(lf -> lf.filter().value())
                                .toList()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existByLink(Long chatId, String link) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        return linkRepository
                .findByUrl(link)
                .map(l -> chatLinkRepository.existsByChatAndLink(chat, l))
                .orElse(false);
    }

    @Override
    @Transactional
    public void saveByLink(Long chatId, AddLinkRequest request) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        LinkEntity link = linkRepository
                .findByUrl(request.link())
                .orElseGet(() -> linkRepository.save(new LinkEntity(null, request.link(), null)));

        ChatLinkEntity chatLink = chatLinkRepository
                .findById(new ChatLinkId(chatId, link.id()))
                .orElseGet(() -> chatLinkRepository.save(
                        new ChatLinkEntity(new ChatLinkId(chatId, link.id()), chat, link, null, null)));

        for (String tag : request.tags()) {
            TagEntity tagEntity =
                    tagRepository.findByName(tag).orElseGet(() -> tagRepository.save(new TagEntity(null, tag)));

            ChatLinkTagEntity chatLinkTag = new ChatLinkTagEntity(
                    new ChatLinkTagId(chatId, link.id(), tagEntity.id()), chat, link, tagEntity, chatLink);
            chatLinkTagRepository.save(chatLinkTag);
        }

        for (String filter : request.filters()) {
            FilterEntity filterEntity = filterRepository
                    .findByValue(filter)
                    .orElseGet(() -> filterRepository.save(new FilterEntity(null, filter)));

            ChatLinkFilterEntity chatLinkFilter = new ChatLinkFilterEntity(
                    new ChatLinkFilterId(chatId, link.id(), filterEntity.id()), chat, link, filterEntity, chatLink);
            chatLinkFilterRepository.save(chatLinkFilter);
        }
    }

    @Transactional
    public LinkResponse deleteByLink(Long chatId, String link) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        LinkEntity linkEntity = linkRepository.findByUrl(link).orElseThrow();

        List<String> tags = chatLinkTagRepository.findAllByChatAndLink(chat, linkEntity).stream()
                .map(ChatLinkTagEntity::tag)
                .map(TagEntity::name)
                .collect(Collectors.toList());

        List<String> filters = chatLinkFilterRepository.findAllByChatAndLink(chat, linkEntity).stream()
                .map(ChatLinkFilterEntity::filter)
                .map(FilterEntity::value)
                .collect(Collectors.toList());

        chatLinkTagRepository.deleteByChatIdAndLinkId(chat.id(), linkEntity.id());
        chatLinkFilterRepository.deleteByChatIdAndLinkId(chat.id(), linkEntity.id());

        chatLinkRepository.deleteByChatIdAndLinkId(chatId, linkEntity.id());

        cleanupDatabase();

        return new LinkResponse(linkEntity.id(), linkEntity.url(), tags, filters);
    }

    @Override
    public List<String> getLinksBatch(int offset, int batchSize) {
        return linkRepository.findAll(PageRequest.of(offset / batchSize, batchSize)).stream()
                .map(LinkEntity::url)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getChatIdsByUrl(String url) {
        return chatLinkRepository.findChatIdsByLinkUrl(url);
    }

    @Override
    public List<String> getTagsByChatId(Long chatId) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow(() -> new TagNotFoundException("Tag not found"));
        return chatLinkTagRepository.findAllByChat(chat).stream()
                .map(clt -> clt.tag().name())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getFiltersByChatId(Long chatId) {
        ChatEntity chat =
                chatRepository.findById(chatId).orElseThrow(() -> new FilterNotFoundException("Filter not found"));
        return chatLinkFilterRepository.findAllByChat(chat).stream()
                .map(clf -> clf.filter().value())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getLinksByTag(String tag, Long chatId) {
        TagEntity tagEntity = tagRepository.findByName(tag).orElseThrow();
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        return chatLinkTagRepository.findAllByChatAndTag(chat, tagEntity).stream()
                .map(clt -> clt.link().url())
                .collect(Collectors.toList());
    }

    @Transactional
    public void cleanupDatabase() {
        linkRepository.cleanupUnusedLinks();
        tagRepository.cleanupUnusedTags();
        filterRepository.cleanupUnusedFilters();
    }
}
