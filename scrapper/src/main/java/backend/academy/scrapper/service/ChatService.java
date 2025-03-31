package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.DbRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ChatService {
    private final DbRepository dbRepository;

    @Transactional
    public void registerChat(Long chatId) {
        if (dbRepository.existById(chatId)) {
            throw new ChatAlreadyExistsException("Chat is already registered.");
        }
        dbRepository.saveById(chatId);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        if (!dbRepository.existById(chatId)) {
            throw new ChatNotFoundException("Chat not found.");
        }
        dbRepository.deleteById(chatId);
    }
}
