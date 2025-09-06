package dev.eduzavarce.pets.shared.core.infrastructure;

import dev.eduzavarce.pets.shared.core.domain.DomainEvent;
import dev.eduzavarce.pets.shared.core.domain.DomainEventDto;
import dev.eduzavarce.pets.shared.core.domain.EventBus;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RabbitMqEventBus implements EventBus {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.routing-prefix:events}")
    private String routingPrefix;

    public RabbitMqEventBus(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) return;
        for (DomainEvent event : events) {
            DomainEventDto dto = event.toPrimitives();
            String routingKey = routingKeyFor(event);
            rabbitTemplate.convertAndSend(routingKey, dto);
        }
    }

    private String routingKeyFor(DomainEvent event) {
        String eventName = event.eventName();
        String aggregate = safe(event.aggregateId());
        String prefix = routingPrefix == null || routingPrefix.isBlank() ? "events" : routingPrefix;
        return String.format("%s.%s.%s", prefix, aggregate, eventName);
    }

    private String safe(String s) {
        if (s == null) return "unknown";
        return s.replaceAll("[^a-zA-Z0-9_.-]", "-");
    }
}
