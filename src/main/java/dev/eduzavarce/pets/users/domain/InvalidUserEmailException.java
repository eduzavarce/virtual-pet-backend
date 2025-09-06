package dev.eduzavarce.pets.users.domain;

import dev.eduzavarce.pets.shared.exceptions.CustomException;

public class InvalidUserEmailException extends CustomException {
    public InvalidUserEmailException(String s) {
        super(s);
    }
}
