package backend.academy.bot.service;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ListStringCacheService {
    private final RedisTemplate<String, List<String>> listRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(10);

    public void setTags(Long chatId, List<String> tags) {
        listRedisTemplate.opsForValue().set("tags:" + chatId, tags, TTL);
    }

    public List<String> getTags(Long chatId) {
        return listRedisTemplate.opsForValue().get("tags:" + chatId);
    }

    public void deleteTags(Long chatId) {
        listRedisTemplate.delete("tags:" + chatId);
    }

    public void setTagLinks(Long chatId, String tag, List<String> links) {
        listRedisTemplate.opsForValue().set("tagLinks:" + chatId + ":" + tag, links, TTL);
    }

    public List<String> getTagLinks(Long chatId, String tag) {
        return listRedisTemplate.opsForValue().get("tagLinks:" + chatId + ":" + tag);
    }

    public void deleteTagLinks(Long chatId, String tag) {
        listRedisTemplate.delete("tagLinks:" + chatId + ":" + tag);
    }
}
