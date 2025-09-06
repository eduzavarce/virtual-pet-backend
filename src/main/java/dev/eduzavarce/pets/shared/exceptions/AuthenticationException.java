package dev.eduzavarce.pets.shared.exceptions;

public class AuthenticationException extends CustomException {
    public AuthenticationException(String message) {
        super(message);
    }
}
