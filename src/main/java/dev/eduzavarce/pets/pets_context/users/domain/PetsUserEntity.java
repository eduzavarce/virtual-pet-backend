package dev.eduzavarce.pets.pets_context.users.domain;

import dev.eduzavarce.pets.shared.core.domain.Entity;

public abstract class PetsUserEntity implements Entity<PetUser> {

  public static PetsUserEntity fromDomain(PetUser petUser) {
    throw new UnsupportedOperationException(
        "This method should be implemented by concrete classes");
  }

  public abstract PetUser toDomain();
}
