package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.exceptions.CustomException;

public class LowStaminaException extends CustomException {
    public LowStaminaException(String message) {
        super(message);
    }
}
