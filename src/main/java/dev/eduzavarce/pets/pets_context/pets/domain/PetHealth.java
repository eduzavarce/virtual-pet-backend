package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.core.domain.IntValueObject;

public class PetHealth extends IntValueObject {
    public PetHealth(Integer value) {
        super(value);
        ensureRange(value);
    }
    private void ensureRange(Integer v) {
        if (v == null) throw new IllegalArgumentException("Health cannot be null");
        if (v < 0 || v > 100) throw new IllegalArgumentException("Health must be between 0 and 100");
    }
}
