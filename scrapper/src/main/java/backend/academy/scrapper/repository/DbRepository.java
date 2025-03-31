package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface DbRepository {
    boolean existById(Long chatId);

    void saveById(Long chatId);

    void deleteById(Long chatId);

    List<LinkResponse> getAllLinksById(Long chatId);

    boolean existByLink(Long chatId, String link);

    void saveByLink(Long chatId, AddLinkRequest request);

    LinkResponse deleteByLink(Long chatId, String link);

    List<String> getAllLinks();

    List<Long> getChatIdsByUrl(String url);

    List<String> getTagsByChatId(Long chatId);

    List<String> getLinksByTag(String tag, Long chatId);
}
