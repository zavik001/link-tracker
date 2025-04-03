package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.ChatLinkTagEntity;
import backend.academy.scrapper.entity.ChatLinkTagId;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.entity.TagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLinkTagRepository extends JpaRepository<ChatLinkTagEntity, ChatLinkTagId> {
    List<ChatLinkTagEntity> findAllByChat(ChatEntity chat);

    List<ChatLinkTagEntity> findAllByChatAndTag(ChatEntity chat, TagEntity tag);

    List<ChatLinkTagEntity> findAllByChatAndLink(ChatEntity chat, LinkEntity link);

    @Modifying
    @Query("DELETE FROM ChatLinkTagEntity ct WHERE ct.chat.id = :chatId AND ct.link.id = :linkId")
    void deleteByChatIdAndLinkId(@Param("chatId") Long chatId, @Param("linkId") Long linkId);
}
