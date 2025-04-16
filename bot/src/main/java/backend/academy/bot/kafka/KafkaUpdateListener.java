package backend.academy.bot.kafka;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.UpdateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUpdateListener {

    private final UpdateService updateService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.dlq}")
    private String dlqTopic;

    @KafkaListener(topics = "${kafka.topics.updates}", groupId = "bot-group")
    public void listen(String message) {
        try {
            LinkUpdate update = new ObjectMapper().readValue(message, LinkUpdate.class);
            log.info("Received update from Kafka: {}", update);
            updateService.sendUpdateToChats(update);
        } catch (Exception e) {
            log.error("❌ Error processing update from Kafka", e);
            try {
                kafkaTemplate.send(dlqTopic, message).get(5, TimeUnit.SECONDS);
            } catch (Exception dlqEx) {
                log.error("Failed to send message to DLQ", dlqEx);
            }
        }
    }
}
