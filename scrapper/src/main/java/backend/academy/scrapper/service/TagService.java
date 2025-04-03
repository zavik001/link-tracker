package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.DbRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TagService {
    private final DbRepository dbRepository;

    public List<String> getTags(Long chatId) {
        return dbRepository.getTagsByChatId(chatId);
    }

    public List<String> getLinksByTag(Long chatId, String tag) {
        return dbRepository.getLinksByTag(tag, chatId);
    }
}
