package com.intellexi.command.events;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange exchange;
    private final String raceRoutingKey;
    private final String applicationRoutingKey;

    public EventPublisher(
            RabbitTemplate rabbitTemplate,
            TopicExchange exchange,
            @Value("${app.rabbit.routing.race}") String raceRoutingKey,
            @Value("${app.rabbit.routing.application}") String applicationRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.raceRoutingKey = raceRoutingKey;
        this.applicationRoutingKey = applicationRoutingKey;
    }

    public void publishRaceEvent(Object payload) {
        rabbitTemplate.convertAndSend(exchange.getName(), raceRoutingKey, payload);
    }

    public void publishApplicationEvent(Object payload) {
        rabbitTemplate.convertAndSend(exchange.getName(), applicationRoutingKey, payload);
    }
}




