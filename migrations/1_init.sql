CREATE TABLE chats (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE links (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    url TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE chat_links (
    chat_id BIGINT NOT NULL,
    link_id BIGINT NOT NULL,
    PRIMARY KEY (chat_id, link_id),
    CONSTRAINT fk_chat_links_chat FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_links_link FOREIGN KEY (link_id) REFERENCES links (id) ON DELETE CASCADE
);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE filters (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    value TEXT UNIQUE NOT NULL
);

CREATE TABLE chat_link_tags (
    chat_id BIGINT NOT NULL,
    link_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (chat_id, link_id, tag_id),
    CONSTRAINT fk_chat_link_tags_chat FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_link_tags_link FOREIGN KEY (link_id) REFERENCES links (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_link_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

CREATE TABLE chat_link_filters (
    chat_id BIGINT NOT NULL,
    link_id BIGINT NOT NULL,
    filter_id BIGINT NOT NULL,
    PRIMARY KEY (chat_id, link_id, filter_id),
    CONSTRAINT fk_chat_link_filters_chat FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_link_filters_link FOREIGN KEY (link_id) REFERENCES links (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_link_filters_filter FOREIGN KEY (filter_id) REFERENCES filters (id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_links_chat ON chat_links (chat_id);
CREATE INDEX idx_chat_links_link ON chat_links (link_id);
CREATE INDEX idx_tags_name ON tags (name);
CREATE INDEX idx_filters_value ON filters (value);
CREATE INDEX idx_chat_link_tags ON chat_link_tags (chat_id, link_id);
CREATE INDEX idx_chat_link_filters ON chat_link_filters (chat_id, link_id);
