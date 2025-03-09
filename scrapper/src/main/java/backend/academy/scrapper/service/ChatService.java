package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.exception.ChatProcessingException;
import backend.academy.scrapper.repository.ChatRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public void registerChat(Long chatId) {
        if (chatRepository.exists(chatId)) {
            throw new ChatAlreadyExistsException("Chat is already registered.");
        }
        try {
            chatRepository.save(new ChatEntity(chatId));
        } catch (Exception e) {
            throw new ChatProcessingException("Chat registration error.");
        }
    }

    public void deleteChat(Long chatId) {
        if (!chatRepository.exists(chatId)) {
            throw new ChatNotFoundException("Chat not found.");
        }
        try {
            chatRepository.delete(chatId);
        } catch (Exception e) {
            throw new ChatProcessingException("Chat deletion error.");
        }
    }
}
