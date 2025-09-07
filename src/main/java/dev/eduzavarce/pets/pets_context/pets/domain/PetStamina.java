package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.core.domain.IntValueObject;

public class PetStamina extends IntValueObject {
    public static final int MIN = 0;
    public static final int MAX = 100;

    public PetStamina(Integer value) {
        super(value);
        ensureRange(value);
    }

    private void ensureRange(Integer v) {
        if (v == null) throw new IllegalArgumentException("Stamina cannot be null");
        if (v < MIN || v > MAX) throw new IllegalArgumentException("Stamina must be between 0 and 100");
    }

    public boolean isDepleted() {
        return this.value() <= MIN;
    }

    public PetStamina decreaseBy(int amount) {
        int newValue = Math.max(MIN, this.value() - Math.max(0, amount));
        return new PetStamina(newValue);
    }

    public PetStamina increase() {
        int amount = 10;
        return new PetStamina(Math.min(MAX, this.value() + amount));
    }

    public PetStamina increaseBy(int amount) {
        int inc = Math.max(0, amount);
        return new PetStamina(Math.min(MAX, this.value() + inc));
    }
}
