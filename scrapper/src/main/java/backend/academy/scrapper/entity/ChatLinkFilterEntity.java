package backend.academy.scrapper.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_link_filters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatLinkFilterEntity {
    @EmbeddedId
    private ChatLinkFilterId id;

    @ManyToOne
    @MapsId("chatId")
    private ChatEntity chat;

    @ManyToOne
    @MapsId("linkId")
    private LinkEntity link;

    @ManyToOne
    @MapsId("filterId")
    private FilterEntity filter;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
        @JoinColumn(name = "link_id", referencedColumnName = "link_id", insertable = false, updatable = false)
    })
    private ChatLinkEntity chatLink;
}
