package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.UpdateEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void removeLink(Long chatId, String url) {
        if (!updates.containsKey(url)) {
            return;
        }

        UpdateEntity entity = updates.get(url);
        entity.removeChatId(chatId);

        if (entity.getChatIds().isEmpty()) {
            updates.remove(url);
        }
    }

    public void removeChatId(Long chatId, List<String> links) {
        if (links == null || links.isEmpty()) {
            return;
        }

        for (String link : links) {
            if (updates.containsKey(link)) {
                UpdateEntity entity = updates.get(link);
                entity.removeChatId(chatId);

                if (entity.getChatIds().isEmpty()) {
                    updates.remove(link);
                }
            }
        }
    }
}
