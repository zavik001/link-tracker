package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.repository.ChatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public String registerChat(Long chatId, String username) {
        if (chatRepository.exists(chatId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chat is already registered.");
        }

        try {
            chatRepository.save(new ChatEntity(chatId, username));
            return "You have successfully registered!";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chat registration error.");
        }
    }

    public String deleteChat(Long chatId) {
        if (!chatRepository.exists(chatId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found.");
        }

        try {
            chatRepository.delete(chatId);
            return "You have successfully deleted your chat.";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chat deletion error.");
        }
    }
}
