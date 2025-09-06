package dev.eduzavarce.pets.auth.users.infrastructure;

import dev.eduzavarce.pets.shared.core.domain.DomainEventDto;
import dev.eduzavarce.pets.shared.core.domain.DomainEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class LogSuccessOnUserCreated implements DomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(LogSuccessOnUserCreated.class);

    @RabbitListener(queues = "${app.rabbitmq.queues.user-created-log:user-created-log.q}")
    public void onMessage(@Payload DomainEventDto event) {
        onEvent(event);
    }

    @Override
    public void onEvent(DomainEventDto event) {
        try {
            if (event == null) {
                log.warn("Received null event on user-created-log queue");
                return;
            }
            if (!"user.created".equals(event.eventName())) {
                // Ignore unrelated events in case of broad binding
                log.debug("Ignoring event with name {} for aggregate {}", event.eventName(), event.aggregateId());
                return;
            }
            log.info("[UserCreated] Received event for aggregateId={}, occurredOn={}, body={}",
                    event.aggregateId(), event.occurredOn(), event.body());
        } catch (Exception e) {
            log.error("Error processing UserCreated event: {}", e.getMessage(), e);
            throw e; // Let listener infrastructure handle retries/DLQ if configured
        }
    }
}
