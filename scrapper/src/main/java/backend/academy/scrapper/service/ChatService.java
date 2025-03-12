package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.exception.ChatProcessingException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.UpdateRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final UpdateRepository updateRepository;

    public ChatService(ChatRepository chatRepository, UpdateRepository updateRepository) {
        this.chatRepository = chatRepository;
        this.updateRepository = updateRepository;
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
            ChatEntity chat = chatRepository.getChat(chatId);
            chatRepository.delete(chatId);
            updateRepository.removeChatId(chatId, chat.links());
        } catch (Exception e) {
            throw new ChatProcessingException("Chat deletion error.");
        }
    }
}
