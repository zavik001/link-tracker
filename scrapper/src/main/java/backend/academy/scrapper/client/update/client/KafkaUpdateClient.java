package backend.academy.scrapper.client.update.client;

import backend.academy.scrapper.dto.UpdateResponse;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transport.type", havingValue = "kafka")
public class KafkaUpdateClient implements UpdateClient {

    private final KafkaTemplate<String, UpdateResponse> kafkaTemplate;

    @Value("${kafka.topics.updates}")
    private String topic;

    @Value("${kafka.topics.dlq}")
    private String dlqTopic;

    @Override
    public void sendUpdate(UpdateResponse updateResponse) {
        try {
            log.info("Sending update to Kafka: {}", updateResponse);
            kafkaTemplate.send(topic, updateResponse).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to send update to main topic", e);
            try {
                log.info("Retrying in DLQ for update: {}", updateResponse);
                kafkaTemplate.send(dlqTopic, updateResponse).get(5, TimeUnit.SECONDS);
            } catch (Exception dlqEx) {
                log.error("Failed to send update to DLQ", dlqEx);
                log.error("CRITICAL: DLQ failed! Lost data: {}", updateResponse);
            }
        }
    }
}
