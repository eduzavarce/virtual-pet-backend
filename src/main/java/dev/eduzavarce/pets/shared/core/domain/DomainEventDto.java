package dev.eduzavarce.pets.shared.core.domain;

public record DomainEventDto(
        String eventName, String occurredOn, String aggregateId, Record body) {}

