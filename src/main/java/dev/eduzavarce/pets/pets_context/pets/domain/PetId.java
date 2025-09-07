package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.shared.core.domain.Identifier;

public class PetId extends Identifier {
    public PetId(String value) {
        super(value);
    }

    protected PetId() {
        super();
    }
}
