package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.ChatRequest;
import backend.academy.scrapper.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<String> registerChat(@RequestBody @Valid ChatRequest chatRequest) {
        return ResponseEntity.ok(chatService.registerChat(chatRequest.chatId(), chatRequest.username()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteChat(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.deleteChat(id));
    }
}
