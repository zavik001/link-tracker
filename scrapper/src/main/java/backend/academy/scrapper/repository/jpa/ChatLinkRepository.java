package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.ChatLinkEntity;
import backend.academy.scrapper.entity.ChatLinkId;
import backend.academy.scrapper.entity.LinkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLinkRepository extends JpaRepository<ChatLinkEntity, ChatLinkId> {
    List<ChatLinkEntity> findAllByChat(ChatEntity chat);

    List<ChatLinkEntity> findAllByLink(LinkEntity link);

    boolean existsByChatAndLink(ChatEntity chat, LinkEntity link);

    @Query("SELECT cl.chat.id FROM ChatLinkEntity cl WHERE cl.link.url = :url")
    List<Long> findChatIdsByLinkUrl(@Param("url") String url);
}
