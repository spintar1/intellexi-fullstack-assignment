package com.intellexi.query.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    TopicExchange eventsExchange(@Value("${app.rabbit.exchange}") String name) {
        return new TopicExchange(name, true, false);
    }

    @Bean
    Queue raceQueue(@Value("${app.rabbit.queues.races}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    Queue applicationQueue(@Value("${app.rabbit.queues.applications}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    Binding raceBinding(@Qualifier("raceQueue") Queue raceQueue, TopicExchange eventsExchange, @Value("${app.rabbit.routing.race}") String routingKey) {
        return BindingBuilder.bind(raceQueue).to(eventsExchange).with(routingKey);
    }

    @Bean
    Binding applicationBinding(@Qualifier("applicationQueue") Queue applicationQueue, TopicExchange eventsExchange, @Value("${app.rabbit.routing.application}") String routingKey) {
        return BindingBuilder.bind(applicationQueue).to(eventsExchange).with(routingKey);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}


