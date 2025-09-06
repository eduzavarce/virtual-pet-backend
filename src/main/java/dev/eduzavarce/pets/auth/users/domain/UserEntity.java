package dev.eduzavarce.pets.auth.users.domain;

import dev.eduzavarce.pets.shared.core.domain.Entity;

public interface UserEntity extends Entity<User> {
    static UserEntity fromDomain(User user) {
        throw new UnsupportedOperationException(
                "This method should be implemented by concrete classes");
    }

    User toDomain();
}
