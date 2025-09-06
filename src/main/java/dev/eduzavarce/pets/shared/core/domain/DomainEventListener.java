package dev.eduzavarce.pets.shared.core.domain;

/**
 * Marker/protocol interface for application services that want to handle domain events.
 * Implementations can be wired to any messaging infrastructure (RabbitMQ, Kafka, etc.).
 */
public interface DomainEventListener {
    /**
     * Handle a domain event represented with primitives/DTO.
     *
     * @param event the event DTO as published on the bus
     */
    void onEvent(DomainEventDto event);
}
