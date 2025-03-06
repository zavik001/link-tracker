package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.ChatEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepository {
    Map<Long, ChatEntity> chats = new HashMap<>();

    public void save(ChatEntity chat) {
        chats.put(chat.id(), chat);
    }

    public ChatEntity findById(Long id) {
        return chats.get(id);
    }

    public boolean exists(Long chatId) {
        return chats.containsKey(chatId);
    }

    public void delete(Long chatId) {
        chats.remove(chatId);
    }
}
