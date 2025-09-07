package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.domain.PetDto;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity(name = "pets")
public class PetPostgresEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private PetsUserPostgresEntity owner;

    @Column(nullable = false)
    private int health;
    @Column(nullable = false)
    private int hunger;
    @Column(nullable = false)
    private int stamina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetType type;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Timestamp createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    protected PetPostgresEntity() {
    }

    public PetPostgresEntity(Pet pet, PetsUserPostgresEntity owner) {
        this.id = pet.getId();
        this.name = pet.getName();
        this.owner = owner;
        this.health = pet.getHealth();
        this.hunger = pet.getHunger();
        this.stamina = pet.getStamina();
        this.type = pet.getType();
    }

    public Pet toDomain() {
        return Pet.fromPrimitives(new PetDto(id, name, owner.getId(), health, hunger, stamina, type));
    }

    public String getId() {
        return id;
    }

    public PetsUserPostgresEntity getOwner() {
        return owner;
    }

}
