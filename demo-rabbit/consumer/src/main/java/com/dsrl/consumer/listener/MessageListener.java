package com.dsrl.consumer.listener;

import com.dsrl.consumer.model.CustomMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {
    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    @RabbitListener(queues = "${app.queue}")
    public void onMessage(CustomMessage message) {
        log.info("Consumer[{}] received → text='{}', sender='{}', ts={}",
                System.getenv("APP_QUEUE"),
                message.text(),
                message.sender(),
                message.timestamp());
    }
}
