package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.core.domain.DomainEvent;

public class PetCreated extends DomainEvent {
    private static final String EVENT_NAME = "pet.created";

    public PetCreated(String aggregateId, Object body) {
        super(aggregateId, EVENT_NAME, body);
    }

    @Override
    public String eventName() {
        return EVENT_NAME;
    }

    @Override
    public DomainEvent fromPrimitives(String aggregateId, Object body, String eventId, String occurredOn) {
        return new PetCreated(aggregateId, body);
    }
}
