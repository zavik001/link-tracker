package backend.academy.bot.service;

import backend.academy.bot.dto.LinkResponse;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateCacheServis {
    private final RedisTemplate<String, List<LinkResponse>> linkRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(10);

    public void setLinks(Long chatId, List<LinkResponse> links) {
        linkRedisTemplate.opsForValue().set("links:" + chatId, links, TTL);
    }

    public List<LinkResponse> getLinks(Long chatId) {
        return linkRedisTemplate.opsForValue().get("links:" + chatId);
    }

    public void deleteLinks(Long chatId) {
        linkRedisTemplate.delete("links:" + chatId);
    }
}
