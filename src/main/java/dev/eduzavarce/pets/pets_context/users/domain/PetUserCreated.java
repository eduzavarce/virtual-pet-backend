package dev.eduzavarce.pets.pets_context.users.domain;

import dev.eduzavarce.pets.shared.core.domain.DomainEvent;

public class PetUserCreated extends DomainEvent {
    private final String eventName;

    public PetUserCreated(String aggregateId, String name, Object body) {
        super(aggregateId, name, body);
        this.eventName = name;
    }

    @Override
    public String eventName() {
        return this.eventName;
    }

    @Override
    public DomainEvent fromPrimitives(String aggregateId, Object body, String eventId, String occurredOn) {
        return new PetUserCreated(aggregateId, eventName, body);
    }
}
