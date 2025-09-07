package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.core.domain.DomainEvent;

public class PetRenamed extends DomainEvent {
    private static final String EVENT_NAME = "pet.renamed";
    public PetRenamed(String aggregateId,PetDto primitives) {
        super(aggregateId, EVENT_NAME, primitives);
    }

    @Override
    public String eventName() {
        return EVENT_NAME;
    }

    @Override
    public DomainEvent fromPrimitives(String aggregateId, Object body, String eventId, String occurredOn) {
        new  PetRenamed(aggregateId, (PetDto) body);
        return this;
    }
}
