package backend.academy.scrapper.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class UpdateEntity {
    private final Long id;
    private final String url;
    private final List<Long> chatIds;

    public UpdateEntity(String url) {
        this.url = url;
        this.id = (long) url.hashCode();
        this.chatIds = new ArrayList<>();
    }

    public void addChatId(Long chatId) {
        if (!chatIds.contains(chatId)) {
            chatIds.add(chatId);
        }
    }
}
