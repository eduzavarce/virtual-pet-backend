package dev.eduzavarce.pets.pets_context.pets.domain;

public record PetDto(
        String id,
        String name,
        String ownerId,
        int health,
        int hunger,
        int stamina
) {}
