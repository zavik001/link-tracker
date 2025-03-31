package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.ChatLinkEntity;
import backend.academy.scrapper.entity.ChatLinkFilterEntity;
import backend.academy.scrapper.entity.ChatLinkFilterId;
import backend.academy.scrapper.entity.FilterEntity;
import backend.academy.scrapper.entity.LinkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLinkFilterRepository extends JpaRepository<ChatLinkFilterEntity, ChatLinkFilterId> {
    List<ChatLinkFilterEntity> findAllByChatLink(ChatLinkEntity chatLink);

    List<ChatLinkFilterEntity> findAllByChat(ChatEntity chat);

    List<ChatLinkFilterEntity> findAllByChatAndLink(ChatEntity chat, LinkEntity link);

    boolean existsByFilter(FilterEntity filter);
}
