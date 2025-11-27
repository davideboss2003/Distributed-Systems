package com.dsrl.producer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitProps {
    @Value("${app.queues.cons1}") public String queueCons1;
    @Value("${app.queues.cons2}") public String queueCons2;

    @Value("${app.exchanges.direct}") public String directEx;
    @Value("${app.exchanges.topic}")  public String topicEx;
    @Value("${app.exchanges.fanout}") public String fanoutEx;

    @Value("${app.routing.direct.cons1}") public String directKeyCons1;
    @Value("${app.routing.direct.cons2}") public String directKeyCons2;

    @Value("${app.routing.topic.cons1}")  public String topicKeyCons1;
    @Value("${app.routing.topic.cons2}")  public String topicKeyCons2;
}
