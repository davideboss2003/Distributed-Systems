package com.dsrl.consumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class RabbitProps {
    private String queue;

    private String directExchange;
    private String directKey;

    private String topicExchange;
    private String topicPattern;

    private String fanoutExchange;

    public String getQueue() { return queue; }
    public void setQueue(String queue) { this.queue = queue; }
    public String getDirectExchange() { return directExchange; }
    public void setDirectExchange(String directExchange) { this.directExchange = directExchange; }
    public String getDirectKey() { return directKey; }
    public void setDirectKey(String directKey) { this.directKey = directKey; }
    public String getTopicExchange() { return topicExchange; }
    public void setTopicExchange(String topicExchange) { this.topicExchange = topicExchange; }
    public String getTopicPattern() { return topicPattern; }
    public void setTopicPattern(String topicPattern) { this.topicPattern = topicPattern; }
    public String getFanoutExchange() { return fanoutExchange; }
    public void setFanoutExchange(String fanoutExchange) { this.fanoutExchange = fanoutExchange; }
}
