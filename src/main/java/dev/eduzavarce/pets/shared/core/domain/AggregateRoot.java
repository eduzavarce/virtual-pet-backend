package dev.eduzavarce.pets.shared.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot {
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public final List<dev.eduzavarce.pets.shared.core.domain.DomainEvent> pullDomainEvents() {
        List<dev.eduzavarce.pets.shared.core.domain.DomainEvent> events = domainEvents;

        domainEvents = Collections.emptyList();

        return events;
    }

    protected final void record(DomainEvent event) {
        domainEvents.add(event);
    }
}
