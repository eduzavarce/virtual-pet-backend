package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.exceptions.CustomException;

public class TooHungryException extends CustomException {
    public TooHungryException(String message) {
        super(message);
    }
}
