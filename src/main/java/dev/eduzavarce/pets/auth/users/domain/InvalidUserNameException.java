package dev.eduzavarce.pets.auth.users.domain;

import dev.eduzavarce.pets.shared.exceptions.CustomException;

public class InvalidUserNameException extends CustomException {
    public InvalidUserNameException(String message) {
        super(message);
    }
}
