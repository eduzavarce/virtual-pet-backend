package dev.eduzavarce.pets.pets_context.users.infrastructure;

import dev.eduzavarce.pets.auth.users.domain.UserDto;
import dev.eduzavarce.pets.pets_context.users.application.CreatePetUserService;
import dev.eduzavarce.pets.shared.core.domain.DomainEventDto;
import dev.eduzavarce.pets.shared.core.domain.DomainEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreatePetsUserOnUserCreated implements DomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(CreatePetsUserOnUserCreated.class);

    private final CreatePetUserService createPetUserService;

    public CreatePetsUserOnUserCreated(CreatePetUserService createPetUserService) {
        this.createPetUserService = createPetUserService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.user-created-pets:user-created-pets.q}")
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
                // Ignore unrelated events just in case of broad binding
                log.debug("Ignoring event with name {} for aggregate {}", event.eventName(), event.aggregateId());
                return;
            }

            Object body = event.body();
            UserDto userDto;
            if (body instanceof UserDto b) {
                userDto = b;
            } else if (body instanceof Map<?, ?> map) {
                // Attempt a safe mapping from a generic Map produced by Jackson
                String id = map.get("id") != null ? String.valueOf(map.get("id")) : null;
                String username = map.get("username") != null ? String.valueOf(map.get("username")) : null;
                String email = map.get("email") != null ? String.valueOf(map.get("email")) : null;
                String role = map.get("role") != null ? String.valueOf(map.get("role")) : null;
                userDto = new UserDto(id, username, email, role);
            } else {
                log.error("Unexpected event body type: {}", body == null ? "null" : body.getClass().getName());
                return;
            }

            createPetUserService.execute(userDto);
        } catch (Exception e) {
            log.error("Error handling user.created event: {}", e.getMessage(), e);
            throw e; // Let listener infrastructure handle retries/DLQ if configured
        }
    }
}
