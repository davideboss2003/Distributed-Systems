package com.dsrl.consumer.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    private final RabbitProps props;

    public RabbitTopologyConfig(RabbitProps props) {
        this.props = props;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(props.getQueue()).build();
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(props.getDirectExchange(), true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(props.getTopicExchange(), true, false);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(props.getFanoutExchange(), true, false);
    }

    @Bean
    public Binding directBinding(Queue q, DirectExchange ex) {
        return BindingBuilder.bind(q).to(ex).with(props.getDirectKey());
    }

    @Bean
    public Binding topicBinding(Queue q, TopicExchange ex) {
        return BindingBuilder.bind(q).to(ex).with(props.getTopicPattern());
    }

    @Bean
    public Binding fanoutBinding(Queue q, FanoutExchange ex) {
        return BindingBuilder.bind(q).to(ex);
    }
}
