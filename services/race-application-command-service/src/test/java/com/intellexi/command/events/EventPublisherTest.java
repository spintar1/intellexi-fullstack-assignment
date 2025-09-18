package com.intellexi.command.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @Mock
    private TopicExchange topicExchange;

    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        when(topicExchange.getName()).thenReturn("events.exchange");
        eventPublisher = new EventPublisher(rabbitTemplate, topicExchange, "race.events", "application.events");
    }

    @Test
    void publishRaceEvent_RaceCreated_ShouldPublishToCorrectExchange() {
        // Given
        UUID raceId = UUID.randomUUID();
        RaceEvents.RaceCreated event = new RaceEvents.RaceCreated(raceId, "Boston Marathon", "Marathon");

        // When
        eventPublisher.publishRaceEvent(event);

        // Then
        ArgumentCaptor<RaceEvents.RaceCreated> eventCaptor = ArgumentCaptor.forClass(RaceEvents.RaceCreated.class);
        verify(rabbitTemplate).convertAndSend(
                eq("events.exchange"),
                eq("race.events"),
                eventCaptor.capture()
        );

        RaceEvents.RaceCreated capturedEvent = eventCaptor.getValue();
        assertEquals(raceId, capturedEvent.getId());
        assertEquals("Boston Marathon", capturedEvent.getName());
        assertEquals("Marathon", capturedEvent.getDistance());
    }

    @Test
    void publishRaceEvent_RaceUpdated_ShouldPublishToCorrectExchange() {
        // Given
        UUID raceId = UUID.randomUUID();
        RaceEvents.RaceUpdated event = new RaceEvents.RaceUpdated(raceId, "Updated Race", "10k");

        // When
        eventPublisher.publishRaceEvent(event);

        // Then
        ArgumentCaptor<RaceEvents.RaceUpdated> eventCaptor = ArgumentCaptor.forClass(RaceEvents.RaceUpdated.class);
        verify(rabbitTemplate).convertAndSend(
                eq("events.exchange"),
                eq("race.events"),
                eventCaptor.capture()
        );

        RaceEvents.RaceUpdated capturedEvent = eventCaptor.getValue();
        assertEquals(raceId, capturedEvent.getId());
        assertEquals("Updated Race", capturedEvent.getName());
        assertEquals("10k", capturedEvent.getDistance());
    }

    @Test
    void publishRaceEvent_RaceDeleted_ShouldPublishToCorrectExchange() {
        // Given
        UUID raceId = UUID.randomUUID();
        RaceEvents.RaceDeleted event = new RaceEvents.RaceDeleted(raceId);

        // When
        eventPublisher.publishRaceEvent(event);

        // Then
        ArgumentCaptor<RaceEvents.RaceDeleted> eventCaptor = ArgumentCaptor.forClass(RaceEvents.RaceDeleted.class);
        verify(rabbitTemplate).convertAndSend(
                eq("events.exchange"),
                eq("race.events"),
                eventCaptor.capture()
        );

        RaceEvents.RaceDeleted capturedEvent = eventCaptor.getValue();
        assertEquals(raceId, capturedEvent.getId());
    }

    @Test
    void publishApplicationEvent_ApplicationCreated_ShouldPublishToCorrectExchange() {
        // Given
        UUID applicationId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        ApplicationEvents.ApplicationCreated event = 
                new ApplicationEvents.ApplicationCreated(applicationId, raceId, "applicant@example.com");

        // When
        eventPublisher.publishApplicationEvent(event);

        // Then
        ArgumentCaptor<ApplicationEvents.ApplicationCreated> eventCaptor = ArgumentCaptor.forClass(ApplicationEvents.ApplicationCreated.class);
        verify(rabbitTemplate).convertAndSend(
                eq("events.exchange"),
                eq("application.events"),
                eventCaptor.capture()
        );

        ApplicationEvents.ApplicationCreated capturedEvent = eventCaptor.getValue();
        assertEquals(applicationId, capturedEvent.getId());
        assertEquals(raceId, capturedEvent.getRaceId());
        assertEquals("applicant@example.com", capturedEvent.getApplicantEmail());
    }

    @Test
    void publishApplicationEvent_ApplicationDeleted_ShouldPublishToCorrectExchange() {
        // Given
        UUID applicationId = UUID.randomUUID();
        ApplicationEvents.ApplicationDeleted event = 
                new ApplicationEvents.ApplicationDeleted(applicationId, "applicant@example.com", "Applicant");

        // When
        eventPublisher.publishApplicationEvent(event);

        // Then
        ArgumentCaptor<ApplicationEvents.ApplicationDeleted> eventCaptor = ArgumentCaptor.forClass(ApplicationEvents.ApplicationDeleted.class);
        verify(rabbitTemplate).convertAndSend(
                eq("events.exchange"),
                eq("application.events"),
                eventCaptor.capture()
        );

        ApplicationEvents.ApplicationDeleted capturedEvent = eventCaptor.getValue();
        assertEquals(applicationId, capturedEvent.getId());
        assertEquals("applicant@example.com", capturedEvent.getApplicantEmail());
        assertEquals("Applicant", capturedEvent.getInitiatorRole());
    }

    @Test
    void publishRaceEvent_RabbitTemplateThrowsException_ShouldPropagateException() {
        // Given
        UUID raceId = UUID.randomUUID();
        RaceEvents.RaceCreated event = new RaceEvents.RaceCreated(raceId, "Test Race", "5k");
        
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventPublisher.publishRaceEvent(event));
    }
}
