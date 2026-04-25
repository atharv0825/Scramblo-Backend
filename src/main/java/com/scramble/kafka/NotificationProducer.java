package com.scramble.kafka;

import com.scramble.dto.Notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {
    private final KafkaTemplate<String , NotificationEvent> kafkaTemplate;

    public void sendEvent(String topic , NotificationEvent event){
        kafkaTemplate.send(topic,event);
    }
}
