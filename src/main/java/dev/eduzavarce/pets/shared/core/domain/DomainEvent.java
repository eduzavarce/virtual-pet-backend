package dev.eduzavarce.pets.shared.core.domain;


import java.time.LocalDateTime;
import java.util.UUID;

public abstract class DomainEvent {
    private final String aggregateId;

    private final String eventId;

    private final String occurredOn;
    private final String eventName;
    private final Object body;

    protected DomainEvent(String aggregateId, String eventName, Object body) {
        this.aggregateId = aggregateId;
        this.eventName = eventName;
        this.body = body;
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = Utils.dateToString(LocalDateTime.now());
    }

    public abstract String eventName();

    public DomainEventDto toPrimitives() {
        return new DomainEventDto(eventName, occurredOn, aggregateId, body);
    }

    public abstract DomainEvent fromPrimitives(
            String aggregateId, Object body, String eventId, String occurredOn);

    public String aggregateId() {
        return aggregateId;
    }

    public String eventId() {
        return eventId;
    }

    public String occurredOn() {
        return occurredOn;
    }
}
