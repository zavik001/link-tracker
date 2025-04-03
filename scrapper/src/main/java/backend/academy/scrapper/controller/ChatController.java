package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.ChatService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/tg-chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/{id}")
    public ResponseEntity<String> registerChat(@PathVariable @Positive Long id) {
        chatService.registerChat(id);
        return ResponseEntity.ok("Chat successfully registered.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteChat(@PathVariable @Positive Long id) {
        chatService.deleteChat(id);
        return ResponseEntity.ok("Chat successfully deleted.");
    }
}
