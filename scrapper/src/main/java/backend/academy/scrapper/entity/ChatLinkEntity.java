package backend.academy.scrapper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_links")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatLinkEntity {
    @EmbeddedId
    private ChatLinkId id;

    @ManyToOne
    @MapsId("chatId")
    private ChatEntity chat;

    @ManyToOne
    @MapsId("linkId")
    private LinkEntity link;

    @OneToMany(mappedBy = "chatLink", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatLinkTagEntity> tags;

    @OneToMany(mappedBy = "chatLink", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatLinkFilterEntity> filters;
}
