package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
@ConditionalOnProperty(name = "database.access-type", havingValue = "SQL")
public class SqlRepository implements DbRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean existById(Long chatId) {
        String sql = "SELECT COUNT(*) FROM chats WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        return count != null && count > 0;
    }

    @Override
    public void saveById(Long chatId) {
        String sql = "INSERT INTO chats (id) VALUES (?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public void deleteById(Long chatId) {
        jdbcTemplate.update("DELETE FROM chat_link_tags WHERE chat_id = ?", chatId);
        jdbcTemplate.update("DELETE FROM chat_link_filters WHERE chat_id = ?", chatId);
        jdbcTemplate.update("DELETE FROM chat_links WHERE chat_id = ?", chatId);
        jdbcTemplate.update("DELETE FROM chats WHERE id = ?", chatId);

        jdbcTemplate.update("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tag_id FROM chat_link_tags)");
        jdbcTemplate.update("DELETE FROM filters WHERE id NOT IN (SELECT DISTINCT filter_id FROM chat_link_filters)");
        jdbcTemplate.update("DELETE FROM links WHERE id NOT IN (SELECT DISTINCT link_id FROM chat_links)");
    }

    @Override
    public List<LinkResponse> getAllLinksById(Long chatId) {
        String sql =
                """
                    SELECT l.id, l.url,
                           COALESCE(array_agg(DISTINCT t.name) FILTER (WHERE t.name IS NOT NULL), '{}') AS tags,
                           COALESCE(array_agg(DISTINCT f.value) FILTER (WHERE f.value IS NOT NULL), '{}') AS filters
                    FROM chat_links cl
                    JOIN links l ON cl.link_id = l.id
                    LEFT JOIN chat_link_tags clt ON cl.chat_id = clt.chat_id AND cl.link_id = clt.link_id
                    LEFT JOIN tags t ON clt.tag_id = t.id
                    LEFT JOIN chat_link_filters clf ON cl.chat_id = clf.chat_id AND cl.link_id = clf.link_id
                    LEFT JOIN filters f ON clf.filter_id = f.id
                    WHERE cl.chat_id = ?
                    GROUP BY l.id, l.url
                """;
        return jdbcTemplate.query(sql, linkResponseMapper, chatId);
    }

    @Override
    public boolean existByLink(Long chatId, String link) {
        String sql =
                """
                    SELECT COUNT(*) FROM chat_links cl
                    JOIN links l ON cl.link_id = l.id
                    WHERE cl.chat_id = ? AND l.url = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId, link);
        return count != null && count > 0;
    }

    @Override
    public void saveByLink(Long chatId, AddLinkRequest request) {
        String sqlLink = "INSERT INTO links (url) VALUES (?) ON CONFLICT (url) DO NOTHING";
        jdbcTemplate.update(sqlLink, request.link());

        String sqlGetLinkId = "SELECT id FROM links WHERE url = ?";
        Long linkId = jdbcTemplate.queryForObject(sqlGetLinkId, Long.class, request.link());

        String sqlChatLink = "INSERT INTO chat_links (chat_id, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sqlChatLink, chatId, linkId);

        for (String tag : request.tags()) {
            String sqlTag = "INSERT INTO tags (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
            jdbcTemplate.update(sqlTag, tag);

            String sqlTagId = "SELECT id FROM tags WHERE name = ?";
            Long tagId = jdbcTemplate.queryForObject(sqlTagId, Long.class, tag);

            String sqlChatLinkTag =
                    "INSERT INTO chat_link_tags (chat_id, link_id, tag_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
            jdbcTemplate.update(sqlChatLinkTag, chatId, linkId, tagId);
        }

        for (String filter : request.filters()) {
            String sqlFilter = "INSERT INTO filters (value) VALUES (?) ON CONFLICT (value) DO NOTHING";
            jdbcTemplate.update(sqlFilter, filter);

            String sqlFilterId = "SELECT id FROM filters WHERE value = ?";
            Long filterId = jdbcTemplate.queryForObject(sqlFilterId, Long.class, filter);

            String sqlChatLinkFilter =
                    "INSERT INTO chat_link_filters (chat_id, link_id, filter_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
            jdbcTemplate.update(sqlChatLinkFilter, chatId, linkId, filterId);
        }
    }

    @Override
    public LinkResponse deleteByLink(Long chatId, String link) {
        String sqlGetLinkId = "SELECT id FROM links WHERE url = ?";
        Long linkId = jdbcTemplate.queryForObject(sqlGetLinkId, Long.class, link);

        String sqlGetTags = "SELECT t.name FROM tags t " + "JOIN chat_link_tags clt ON t.id = clt.tag_id "
                + "WHERE clt.chat_id = ? AND clt.link_id = ?";
        List<String> tags = jdbcTemplate.queryForList(sqlGetTags, String.class, chatId, linkId);

        String sqlGetFilters = "SELECT f.name FROM filters f " + "JOIN chat_link_filters clf ON f.id = clf.filter_id "
                + "WHERE clf.chat_id = ? AND clf.link_id = ?";
        List<String> filters = jdbcTemplate.queryForList(sqlGetFilters, String.class, chatId, linkId);

        jdbcTemplate.update("DELETE FROM chat_link_tags WHERE chat_id = ? AND link_id = ?", chatId, linkId);
        jdbcTemplate.update("DELETE FROM chat_link_filters WHERE chat_id = ? AND link_id = ?", chatId, linkId);
        jdbcTemplate.update("DELETE FROM chat_links WHERE chat_id = ? AND link_id = ?", chatId, linkId);

        jdbcTemplate.update("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tag_id FROM chat_link_tags)");
        jdbcTemplate.update("DELETE FROM filters WHERE id NOT IN (SELECT DISTINCT filter_id FROM chat_link_filters)");
        jdbcTemplate.update("DELETE FROM links WHERE id NOT IN (SELECT DISTINCT link_id FROM chat_links)");

        return new LinkResponse(linkId, link, tags, filters);
    }

    @Override
    public List<String> getAllLinks() {
        String sql = "SELECT url FROM links";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public List<Long> getChatIdsByUrl(String url) {
        String sql = "SELECT chat_id FROM chat_links cl JOIN links l ON cl.link_id = l.id WHERE l.url = ?";
        return jdbcTemplate.queryForList(sql, Long.class, url);
    }

    @Override
    public List<String> getTagsByChatId(Long chatId) {
        String sql =
                """
                    SELECT DISTINCT t.name
                    FROM tags t
                    JOIN chat_link_tags clt ON t.id = clt.tag_id
                    WHERE clt.chat_id = ?
                """;
        return jdbcTemplate.queryForList(sql, String.class, chatId);
    }

    @Override
    public List<String> getLinksByTag(String tag, Long chatId) {
        String sql =
                """
                    SELECT DISTINCT l.url
                    FROM links l
                    JOIN chat_link_tags clt ON l.id = clt.link_id
                    JOIN tags t ON clt.tag_id = t.id
                    WHERE clt.chat_id = ? AND t.name = ?
                """;
        return jdbcTemplate.queryForList(sql, String.class, chatId, tag);
    }

    private final RowMapper<LinkResponse> linkResponseMapper = (rs, rowNum) -> new LinkResponse(
            rs.getLong("id"),
            rs.getString("url"),
            List.of((String[]) rs.getArray("tags").getArray()),
            List.of((String[]) rs.getArray("filters").getArray()));
}
