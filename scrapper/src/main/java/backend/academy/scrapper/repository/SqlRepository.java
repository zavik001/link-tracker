package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "database.access-type", havingValue = "SQL")
public class SqlRepository implements DbRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public boolean existById(Long chatId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM chats WHERE id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, chatId));
    }

    @Override
    public void saveById(Long chatId) {
        String sql = "INSERT INTO chats (id) VALUES (?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public void deleteById(Long chatId) {
        jdbcTemplate.update("DELETE FROM chats WHERE id = ?", chatId);
        cleanupDatabase();
    }

    @Override
    public boolean existByLink(Long chatId, String link) {
        String sql =
                """
                SELECT EXISTS (
                    SELECT 1 FROM chat_links cl
                    JOIN links l ON cl.link_id = l.id
                    WHERE cl.chat_id = ? AND l.url = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, chatId, link));
    }

    @Override
    public void saveByLink(Long chatId, AddLinkRequest request) {
        String sqlLink = "INSERT INTO links (url) VALUES (?) ON CONFLICT (url) DO NOTHING";
        jdbcTemplate.update(sqlLink, request.link());

        String sqlGetLinkId = "SELECT id FROM links WHERE url = ?";
        Long linkId = jdbcTemplate.queryForObject(sqlGetLinkId, Long.class, request.link());

        String sqlChatLink = "INSERT INTO chat_links (chat_id, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sqlChatLink, chatId, linkId);

        batchInsertTags(request.tags(), chatId, linkId);
        batchInsertFilters(request.filters(), chatId, linkId);
    }

    @Override
    public LinkResponse deleteByLink(Long chatId, String link) {
        String sqlGetLinkId = "SELECT id FROM links WHERE url = ?";
        Long linkId = jdbcTemplate.queryForObject(sqlGetLinkId, Long.class, link);

        String sqlDeleteChatLink =
                """
                DELETE FROM chat_links
                WHERE chat_id = ? AND link_id = ?
                RETURNING link_id
                """;
        Long deletedLinkId = jdbcTemplate.queryForObject(sqlDeleteChatLink, Long.class, chatId, linkId);

        String sqlDeleteChatLinkTags =
                """
                DELETE FROM chat_link_tags
                WHERE chat_id = ? AND link_id = ?
                """;
        jdbcTemplate.update(sqlDeleteChatLinkTags, chatId, linkId);

        String sqlDeleteChatLinkFilters =
                """
                DELETE FROM chat_link_filters
                WHERE chat_id = ? AND link_id = ?
                """;
        jdbcTemplate.update(sqlDeleteChatLinkFilters, chatId, linkId);

        if (deletedLinkId == null) {
            return new LinkResponse(linkId, link, List.of(), List.of());
        }

        List<String> tags = getTagsByChatId(chatId);
        List<String> filters = getFiltersByChatId(chatId);

        cleanupDatabase();
        return new LinkResponse(linkId, link, tags, filters);
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
                LEFT JOIN chat_link_tags clt ON cl.link_id = clt.link_id AND cl.chat_id = clt.chat_id
                LEFT JOIN tags t ON clt.tag_id = t.id
                LEFT JOIN chat_link_filters clf ON cl.link_id = clf.link_id AND cl.chat_id = clf.chat_id
                LEFT JOIN filters f ON clf.filter_id = f.id
                WHERE cl.chat_id = ?
                GROUP BY l.id, l.url
                """;
        return jdbcTemplate.query(sql, linkResponseMapper, chatId);
    }

    @Override
    public List<String> getLinksBatch(int offset, int batchSize) {
        String sql = "SELECT url FROM links ORDER BY id LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, String.class, batchSize, offset);
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
    public List<String> getFiltersByChatId(Long chatId) {
        String sql =
                """
                    SELECT DISTINCT f.value
                    FROM filters f
                    JOIN chat_link_filters clf ON f.id = clf.filter_id
                    WHERE clf.chat_id = ?
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

    private void cleanupDatabase() {
        String sqlCleanup =
                """
                DELETE FROM links WHERE id NOT IN (SELECT DISTINCT link_id FROM chat_links);
                DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tag_id FROM chat_link_tags);
                DELETE FROM filters WHERE id NOT IN (SELECT DISTINCT filter_id FROM chat_link_filters);
                """;
        jdbcTemplate.update(sqlCleanup);
    }

    private void batchInsertTags(List<String> tags, Long chatId, Long linkId) {
        if (tags.isEmpty()) return;

        String sqlTags = "INSERT INTO tags (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        jdbcTemplate.batchUpdate(sqlTags, tags, tags.size(), (ps, tag) -> ps.setString(1, tag));

        SqlParameterSource parameters = new MapSqlParameterSource("names", tags);
        String sqlTagIds = "SELECT id, name FROM tags WHERE name IN (:names)";

        Map<String, Long> tagIds = namedParameterJdbcTemplate.query(sqlTagIds, parameters, rs -> {
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("name"), rs.getLong("id"));
            }
            return result;
        });

        String sqlChatLinkTags =
                "INSERT INTO chat_link_tags (chat_id, link_id, tag_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.batchUpdate(sqlChatLinkTags, tags, tags.size(), (ps, tag) -> {
            ps.setLong(1, chatId);
            ps.setLong(2, linkId);
            ps.setLong(3, tagIds.get(tag));
        });
    }

    private void batchInsertFilters(List<String> filters, Long chatId, Long linkId) {
        if (filters.isEmpty()) return;

        String sqlFilters = "INSERT INTO filters (value) VALUES (?) ON CONFLICT (value) DO NOTHING";
        jdbcTemplate.batchUpdate(sqlFilters, filters, filters.size(), (ps, filter) -> ps.setString(1, filter));

        SqlParameterSource parameters = new MapSqlParameterSource("values", filters);
        String sqlFilterIds = "SELECT id, value FROM filters WHERE value IN (:values)";

        Map<String, Long> filterIds = namedParameterJdbcTemplate.query(sqlFilterIds, parameters, rs -> {
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("value"), rs.getLong("id"));
            }
            return result;
        });

        String sqlChatLinkFilters =
                "INSERT INTO chat_link_filters (chat_id, link_id, filter_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.batchUpdate(sqlChatLinkFilters, filters, filters.size(), (ps, filter) -> {
            ps.setLong(1, chatId);
            ps.setLong(2, linkId);
            ps.setLong(3, filterIds.get(filter));
        });
    }

    private final RowMapper<LinkResponse> linkResponseMapper = (rs, rowNum) -> new LinkResponse(
            rs.getLong("id"),
            rs.getString("url"),
            List.of((String[]) rs.getArray("tags").getArray()),
            List.of((String[]) rs.getArray("filters").getArray()));

    @Override
    public List<String> getFiltersByChatIdAndLink(Long chatId, String link) {
        String sqlGetLinkId = "SELECT id FROM links WHERE url = ?";
        Long linkId = jdbcTemplate.queryForObject(sqlGetLinkId, Long.class, link);
        String sql =
                """
                SELECT f.value FROM filters f
                JOIN chat_link_filters clf ON f.id = clf.filter_id
                WHERE clf.chat_id = ? AND clf.link_id = ?
                """;

        return jdbcTemplate.queryForList(sql, String.class, chatId, linkId);
    }
}
