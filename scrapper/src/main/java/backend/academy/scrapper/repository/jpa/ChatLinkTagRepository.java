package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.ChatLinkEntity;
import backend.academy.scrapper.entity.ChatLinkTagEntity;
import backend.academy.scrapper.entity.ChatLinkTagId;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.entity.TagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLinkTagRepository extends JpaRepository<ChatLinkTagEntity, ChatLinkTagId> {
    List<ChatLinkTagEntity> findAllByChatLink(ChatLinkEntity chatLink);

    List<ChatLinkTagEntity> findAllByChat(ChatEntity chat);

    List<ChatLinkTagEntity> findAllByChatAndTag(ChatEntity chat, TagEntity tag);

    List<ChatLinkTagEntity> findAllByChatAndLink(ChatEntity chat, LinkEntity link);

    boolean existsByTag(TagEntity tag);
}
