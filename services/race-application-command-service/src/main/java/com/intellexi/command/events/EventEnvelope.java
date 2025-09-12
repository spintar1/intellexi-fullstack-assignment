package com.intellexi.command.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EventEnvelope {
    private UUID eventId;
    private String eventType;
    private Instant occurredAt;
    private Object payload;
    private Map<String, Object> meta;

    public EventEnvelope() {}

    public EventEnvelope(UUID eventId, String eventType, Instant occurredAt, Object payload, Map<String, Object> meta) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.payload = payload;
        this.meta = meta;
    }

    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Instant getOccurredAt() { return occurredAt; }
    public Object getPayload() { return payload; }
    public Map<String, Object> getMeta() { return meta; }
}


