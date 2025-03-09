package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.ChatService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/tg-chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> registerChat(@PathVariable @Positive @Max(Long.MAX_VALUE) Long id) {
        chatService.registerChat(id);
        return ResponseEntity.ok("Chat successfully registered.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteChat(@PathVariable @Positive @Max(Long.MAX_VALUE) Long id) {
        chatService.deleteChat(id);
        return ResponseEntity.ok("Chat successfully deleted.");
    }
}
