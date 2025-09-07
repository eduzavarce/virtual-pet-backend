package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.core.domain.IntValueObject;

public class PetStamina extends IntValueObject {
    public PetStamina(Integer value) {
        super(value);
        ensureRange(value);
    }
    private void ensureRange(Integer v) {
        if (v == null) throw new IllegalArgumentException("Stamina cannot be null");
        if (v < 0 || v > 100) throw new IllegalArgumentException("Stamina must be between 0 and 100");
    }
    public PetStamina increase() {
        int amount = 10;
        return new PetStamina(Math.min(100, this.value() + amount));
    }
}
