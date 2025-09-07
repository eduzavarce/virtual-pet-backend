package dev.eduzavarce.pets.pets_context.pets.domain;

import dev.eduzavarce.pets.auth.users.domain.UserId;
import dev.eduzavarce.pets.shared.core.domain.AggregateRoot;

public class Pet extends AggregateRoot {
    private final PetId id;
    private final UserId ownerId;
    private final PetType type;
    private PetName name;
    private PetHealth health;
    private PetHunger hunger;
    private PetStamina stamina;

    private Pet(String id, String name, String ownerId, int health, int hunger, int stamina, PetType type) {
        this.id = new PetId(id);
        this.name = new PetName(name);
        this.ownerId = new UserId(ownerId);
        this.health = new PetHealth(health);
        this.hunger = new PetHunger(hunger);
        this.stamina = new PetStamina(stamina);
        this.type = type;
    }

    public static Pet create(PetDto dto) {
        var pet = fromPrimitives(dto);
        pet.record(new PetCreated(pet.id.value(), pet.toPrimitives()));
        return pet;
    }

    public static Pet fromPrimitives(PetDto dto) {
        return new Pet(dto.id(), dto.name(), dto.ownerId(), dto.health(), dto.hunger(), dto.stamina(), dto.type());
    }

    public PetDto toPrimitives() {
        return new PetDto(id.value(), name.value(), ownerId.value(), health.value(), hunger.value(), stamina.value(), type);
    }

    public void rename(String newName) {
        PetName updatedName = new PetName(newName);
        if (!this.name.equals(updatedName)) {
            this.name = updatedName;
            this.record(new PetRenamed(this.id.value(), this.toPrimitives()));
        }
    }

    public String getId() {
        return id.value();
    }

    public String getName() {
        return name.value();
    }

    public String getOwnerId() {
        return ownerId.value();
    }

    public int getHealth() {
        return health.value();
    }

    public int getHunger() {
        return hunger.value();
    }

    public int getStamina() {
        return stamina.value();
    }

    public PetType getType() {
        return type;
    }
}
