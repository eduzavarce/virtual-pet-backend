package dev.eduzavarce.pets.pets_context.pets.domain;

public record PetWithOwnerDto(
        String id,
        String name,
        String ownerId,
        String ownerUsername,
        int health,
        int hunger,
        int stamina,
        PetType type
) {
}
