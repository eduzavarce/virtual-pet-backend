package dev.eduzavarce.pets.auth.users.domain;

public record CreateUserDto(
        String id,
        String username,
        String email,
        String password
) {
    public CreateUserDto {
        if (password == null || password.isBlank()) {
            throw new InvalidPasswordException();
        }
    }
}
