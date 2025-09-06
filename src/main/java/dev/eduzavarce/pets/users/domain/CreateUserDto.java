package dev.eduzavarce.pets.users.domain;

public record CreateUserDto(
        String id,
        String username,
                            String email,
                            String password,
                            String role) {
    public CreateUserDto {
        if (password == null || password.isBlank()) {
            throw new InvalidPasswordException();
        }
    }
}
