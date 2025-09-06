package dev.eduzavarce.pets.shared.core.domain;

import java.util.List;

public interface EventBus {
  void publish(final List<dev.eduzavarce.pets.shared.core.domain.DomainEvent> events);
}
