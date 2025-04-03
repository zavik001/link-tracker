package backend.academy.scrapper.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatLinkFilterId implements Serializable {
    private Long chatId;
    private Long linkId;
    private Long filterId;
}
