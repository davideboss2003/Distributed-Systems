package com.energy.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurare RabbitMQ
 * 
 * Acest config face următoarele:
 * 1. Declară queue-ul "overconsumption_queue"
 * 2. Configurează JSON converter pentru mesaje
 * 3. Configurează RabbitTemplate
 */
@Configuration
public class RabbitMQConfig {
    
    @Value("${app.rabbitmq.overconsumption-queue}")
    private String overconsumptionQueue;
    
    /**
     * Declară queue-ul pentru overconsumption events
     * durable=true înseamnă că queue-ul supraviețuiește restart-ului RabbitMQ
     */
    @Bean
    public Queue overconsumptionQueue() {
        return new Queue(overconsumptionQueue, true);
    }
    
    /**
     * Declară queue-ul pentru chat messages
     */
    @Bean
    public Queue chatQueue() {
        return new Queue("chat_queue", true);
    }
    
    /**
     * Converter pentru serializare/deserializare JSON
     * Convertește automat între Java objects și JSON
     * Cu suport pentru Java 8 Date/Time (LocalDateTime)
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    /**
     * RabbitTemplate cu JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
