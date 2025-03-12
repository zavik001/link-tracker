package backend.academy.scrapper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AddLinkRequest(
        @NotBlank(message = "Link cannot be empty") String link,
        List<@Size(min = 1, message = "Tags must have at least one character") String> tags,
        List<@Size(min = 1, message = "Filters must have at least one character") String> filters) {
    public AddLinkRequest(String link, List<String> tags, List<String> filters) {
        this.link = link;
        this.tags = tags != null ? tags : List.of();
        this.filters = filters != null ? filters : List.of();
    }
}
