package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.UpdateEntity;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class UpdateRepository {
    private final Map<String, UpdateEntity> updates = new HashMap<>();

    public void addLink(Long chatId, String url) {
        updates.computeIfAbsent(url, UpdateEntity::new).addChatId(chatId);
    }

    public List<String> getAllLinks() {
        return new ArrayList<>(updates.keySet());
    }

    public List<Long> getChatIdsByUrl(String url) {
        return updates.getOrDefault(url, new UpdateEntity(url)).getChatIds();
    }
}
