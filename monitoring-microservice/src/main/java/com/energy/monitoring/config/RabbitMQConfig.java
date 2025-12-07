package com.energy.monitoring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Value("${app.rabbitmq.sync-queue}")
    private String syncQueueName;
    
    @Value("${app.rabbitmq.sensor-data-queue}")
    private String sensorDataQueueName;
    
    @Value("${app.rabbitmq.overconsumption-queue:overconsumption_queue}")
    private String overconsumptionQueueName;
    
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
    
    @Bean
    public Queue syncQueue() {
        return QueueBuilder.durable(syncQueueName).build();
    }
    
    @Bean
    public Queue sensorDataQueue() {
        return QueueBuilder.durable(sensorDataQueueName).build();
    }
    
    @Bean
    public Queue overconsumptionQueue() {
        return QueueBuilder.durable(overconsumptionQueueName).build();
    }
}
