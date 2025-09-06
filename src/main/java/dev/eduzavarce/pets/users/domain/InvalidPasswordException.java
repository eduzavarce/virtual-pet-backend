package dev.eduzavarce.pets.users.domain;

import dev.eduzavarce.pets.shared.exceptions.CustomException;

public class InvalidPasswordException extends CustomException {
    public static final String DEFAULT_MESSAGE = "Password must be at least 8 characters, include upper and lower case letters, at least one number, one special symbol, and contain no spaces.";

    public InvalidPasswordException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidPasswordException(String message) {
        super(message);
    }
}
