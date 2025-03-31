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

    @Override
    @Transactional
    public LinkResponse deleteByLink(Long chatId, String link) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        LinkEntity linkEntity = linkRepository.findByUrl(link).orElseThrow();
        ChatLinkEntity chatLink = chatLinkRepository
                .findById(new ChatLinkId(chat.id(), linkEntity.id()))
                .orElseThrow();

        List<ChatLinkTagEntity> chatLinkTags = chatLinkTagRepository.findAllByChatAndLink(chat, linkEntity);
        List<ChatLinkFilterEntity> chatLinkFilters = chatLinkFilterRepository.findAllByChatAndLink(chat, linkEntity);

        chatLinkTagRepository.deleteAll(chatLinkTags);
        chatLinkFilterRepository.deleteAll(chatLinkFilters);
        chatLinkRepository.delete(chatLink);

        for (ChatLinkTagEntity chatLinkTag : chatLinkTags) {
            TagEntity tag = chatLinkTag.tag();
            if (!chatLinkTagRepository.existsByTag(tag)) {
                tagRepository.delete(tag);
            }
        }

        for (ChatLinkFilterEntity chatLinkFilter : chatLinkFilters) {
            FilterEntity filter = chatLinkFilter.filter();
            if (!chatLinkFilterRepository.existsByFilter(filter)) {
                filterRepository.delete(filter);
            }
        }

        if (chatLinkRepository.findAllByLink(linkEntity).isEmpty()) {
            linkRepository.delete(linkEntity);
        }

        return new LinkResponse(
                linkEntity.id(),
                linkEntity.url(),
                chatLinkTags.stream()
                        .map(ChatLinkTagEntity::tag)
                        .map(TagEntity::name)
                        .collect(Collectors.toList()),
                chatLinkFilters.stream()
                        .map(ChatLinkFilterEntity::filter)
                        .map(FilterEntity::value)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<String> getAllLinks() {
        return linkRepository.findAll().stream().map(LinkEntity::url).collect(Collectors.toList());
    }

    @Override
    public List<Long> getChatIdsByUrl(String url) {
        return chatLinkRepository.findChatIdsByLinkUrl(url);
    }

    @Override
    public List<String> getTagsByChatId(Long chatId) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        return chatLinkTagRepository.findAllByChat(chat).stream()
                .map(clt -> clt.tag().name())
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
}
