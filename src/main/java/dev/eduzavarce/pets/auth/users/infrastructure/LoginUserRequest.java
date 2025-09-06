package dev.eduzavarce.pets.auth.users.infrastructure;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginUserRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {

}
