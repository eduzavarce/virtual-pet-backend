package dev.eduzavarce.pets.pets_context.users.domain;

import dev.eduzavarce.pets.auth.users.domain.UserId;
import dev.eduzavarce.pets.auth.users.domain.Username;
import dev.eduzavarce.pets.shared.core.domain.AggregateRoot;

public class PetUser extends AggregateRoot {
    private final UserId id;
    private final Username username;

    private PetUser(String id, String username) {
        this.id = new UserId(id);
        this.username = new Username(username);
    }

    public static PetUser create(PetUserDto petUserDto) {
        var petUser = PetUser.fromPrimitives(petUserDto);
        var event = new PetUserCreated(petUser.id.value(), "pets.users.created", petUser.toPrimitives());
        petUser.record(event);
        return petUser;
    }

    public static PetUser fromPrimitives(PetUserDto petUserDto) {
        return new PetUser(petUserDto.id(), petUserDto.username());
    }

    public PetUserDto toPrimitives() {
        return new PetUserDto(this.id.value(), this.username.value());
    }

}
