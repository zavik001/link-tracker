package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.ChatLinkFilterEntity;
import backend.academy.scrapper.entity.ChatLinkFilterId;
import backend.academy.scrapper.entity.LinkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLinkFilterRepository extends JpaRepository<ChatLinkFilterEntity, ChatLinkFilterId> {
    List<ChatLinkFilterEntity> findAllByChat(ChatEntity chat);

    List<ChatLinkFilterEntity> findAllByChatAndLink(ChatEntity chat, LinkEntity link);

    @Modifying
    @Query("DELETE FROM ChatLinkFilterEntity cf WHERE cf.chat.id = :chatId AND cf.link.id = :linkId")
    void deleteByChatIdAndLinkId(@Param("chatId") Long chatId, @Param("linkId") Long linkId);
}
