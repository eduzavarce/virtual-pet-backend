package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.auth.users.domain.UserId;
import dev.eduzavarce.pets.shared.core.domain.AggregateRoot;

public class Pet extends AggregateRoot {
    private final PetId id;
    private final PetName name;
    private final UserId ownerId;
    private final PetHealth health;
    private final PetHunger hunger;
    private final PetStamina stamina;

    private Pet(String id, String name, String ownerId, int health, int hunger, int stamina) {
        this.id = new PetId(id);
        this.name = new PetName(name);
        this.ownerId = new UserId(ownerId);
        this.health = new PetHealth(health);
        this.hunger = new PetHunger(hunger);
        this.stamina = new PetStamina(stamina);
    }

    public static Pet create(PetDto dto) {
        var pet = fromPrimitives(dto);
        pet.record(new PetCreated(pet.id.value(), pet.toPrimitives()));
        return pet;
    }

    public static Pet fromPrimitives(PetDto dto) {
        return new Pet(dto.id(), dto.name(), dto.ownerId(), dto.health(), dto.hunger(), dto.stamina());
    }

    public PetDto toPrimitives() {
        return new PetDto(id.value(), name.value(), ownerId.value(), health.value(), hunger.value(), stamina.value());
    }

    public String getId() { return id.value(); }
    public String getName() { return name.value(); }
    public String getOwnerId() { return ownerId.value(); }
    public int getHealth() { return health.value(); }
    public int getHunger() { return hunger.value(); }
    public int getStamina() { return stamina.value(); }
}
