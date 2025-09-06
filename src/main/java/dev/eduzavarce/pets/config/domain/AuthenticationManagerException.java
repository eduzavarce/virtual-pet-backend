package dev.eduzavarce.pets.config.domain;

import dev.eduzavarce.pets.shared.exceptions.CustomException;

public class AuthenticationManagerException extends CustomException {
    public AuthenticationManagerException(String message) {
        super(message);
    }
}
