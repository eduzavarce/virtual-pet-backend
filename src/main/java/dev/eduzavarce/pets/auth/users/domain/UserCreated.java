package dev.eduzavarce.pets.auth.users.domain;

import dev.eduzavarce.pets.shared.core.domain.DomainEvent;

public class UserCreated extends DomainEvent {
    private final String eventName;

    public UserCreated(String aggregateId, String name, Object body) {
        super(aggregateId, name, body);
        this.eventName = name;
    }

    @Override
    public String eventName() {
        return this.eventName;
    }

    @Override
    public DomainEvent fromPrimitives(String aggregateId, Object body, String eventId, String occurredOn) {
        // Accept any body representation (e.g., Map) without strict casting for logging use case
        return new UserCreated(aggregateId, eventName, body);
    }
}
