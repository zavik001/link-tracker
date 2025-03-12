package backend.academy.scrapper.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class UpdateEntity {
    private final String url;
    private final List<Long> chatIds;

    public UpdateEntity(String url) {
        this.url = url;
        this.chatIds = new ArrayList<>();
    }

    public void addChatId(Long chatId) {
        if (!chatIds.contains(chatId)) {
            chatIds.add(chatId);
        }
    }

    public List<Long> getChatIds() {
        return new ArrayList<>(chatIds);
    }

    public void removeChatId(Long chatId) {
        chatIds.remove(chatId);
    }
}
