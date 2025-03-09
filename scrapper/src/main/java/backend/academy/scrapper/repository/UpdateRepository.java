package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.UpdateEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class UpdateRepository {
    Map<String, UpdateEntity> updates = new HashMap<>();

    public void addLink(Long chatId, String url) {
        updates.computeIfAbsent(url, UpdateEntity::new).addChatId(chatId);
    }
}
