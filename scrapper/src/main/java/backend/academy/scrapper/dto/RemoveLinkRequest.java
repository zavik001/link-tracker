package backend.academy.scrapper.dto;

import jakarta.validation.constraints.NotBlank;

public record RemoveLinkRequest(@NotBlank(message = "Link cannot be empty") String link) {}
