package dev.eduzavarce.pets.auth.users.domain;

import dev.eduzavarce.pets.shared.core.domain.StringValueObject;

public class Username extends StringValueObject {
    public Username(String value) {
        super(value);
        validate(value);
    }

    private void validate(String value) {
        if (value == null || value.trim().isEmpty() || value.length() > 20) {
            throw new InvalidUserNameException("Invalid user name: " + value);
        }
    }
}
