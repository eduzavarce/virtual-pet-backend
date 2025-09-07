package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.core.domain.StringValueObject;

public class PetName extends StringValueObject {
    public PetName(String value) {
        super(value);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Pet name cannot be empty");
        if (value.trim().length() > 30) throw new IllegalArgumentException("Pet name too long (max 30)");
    }
}
