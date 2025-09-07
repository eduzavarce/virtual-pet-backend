package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.core.domain.IntValueObject;

public class PetHunger extends IntValueObject {
    public static final int MIN = 0;
    public static final int MAX = 100;

    public PetHunger(Integer value) {
        super(value);
        ensureRange(value);
    }

    private void ensureRange(Integer v) {
        if (v == null) throw new IllegalArgumentException("Hunger cannot be null");
        if (v < MIN || v > MAX) throw new IllegalArgumentException("Hunger must be between 0 and 100");
    }

    public boolean isMaxed() {
        return this.value() >= MAX;
    }

    public PetHunger decreaseBy(int amount) {
        int newValue = Math.max(MIN, this.value() - Math.max(0, amount));
        return new PetHunger(newValue);
    }

    public PetHunger increaseBy(int amount) {
        int newValue = Math.min(MAX, this.value() + Math.max(0, amount));
        return new PetHunger(newValue);
    }

    public PetHunger feed() {
        // Decrease hunger by 10 when feeding
        return decreaseBy(10);
    }
}
