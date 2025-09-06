package dev.eduzavarce.pets.users.domain;

import dev.eduzavarce.pets.shared.core.domain.DomainEvent;

public class UserCreated extends DomainEvent {
    private final String eventName;
    public UserCreated(String aggregateId, String name, Record body) {
        super(aggregateId, name, body);
        this.eventName = name;
    }

    @Override
    public String eventName() {
        return this.eventName;
    }

    @Override
    public DomainEvent fromPrimitives(String aggregateId, Record body, String eventId, String occurredOn) {
        if (!(body instanceof UserDto userDto)) {
            throw new IllegalArgumentException("body is not a UserDto");
        }
        return new UserCreated(aggregateId, eventName, userDto);
    }
}
