package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/tags")
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<String>> getTags(@RequestHeader("Tg-Chat-Id") @Positive Long chatId) {
        return ResponseEntity.ok(tagService.getTags(chatId));
    }

    @PostMapping
    public ResponseEntity<List<String>> getLinksByTag(
            @RequestHeader("Tg-Chat-Id") @Positive Long chatId, @Valid @RequestBody RemoveLinkRequest tag) {
        return ResponseEntity.ok(tagService.getLinksByTag(chatId, tag.link()));
    }
}
