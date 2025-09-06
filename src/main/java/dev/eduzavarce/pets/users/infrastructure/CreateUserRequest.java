package dev.eduzavarce.pets.users.infrastructure;

import dev.eduzavarce.pets.shared.core.infrastructure.ValidUUID;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Schema(name = "CreateUserRequest", description = "Payload to create a new user")
public record CreateUserRequest(
        @NotBlank
        @ValidUUID
        @Schema(description = "User unique identifier (UUID v4)", example = "3f2a0c83-6c2a-4c3a-a3b3-9f1a2b2c3d4e", format = "uuid")
        String id,

        @NotBlank
        @Size(min = 3, max = 20)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Username may contain only letters, digits, hyphens (-), and underscores (_).")
        @Schema(description = "Public username used to login and display", minLength = 3, maxLength = 20, example = "john_doe")
        String username,
        @NotBlank
        @Email
        @Schema(description = "User email address", example = "john.doe@example.com", format = "email")
        String email,
        @NotBlank
        @Size(min = 8, max = 30)
        @Pattern(
                regexp = "^(?=\\S+$)(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%&*!?^()_+=\\-{}\\[\\]:;\\\\\\|\\'\"<>,./~`]).{8,}$",
                message = "Password must be at between 8 and 30 characters, include upper and lower case letters, at least one number, one special symbol, and contain no spaces."
        )
        @Schema(description = "Account password", example = "Str0ngP@ssw0rd!", minLength = 8, maxLength = 30)
        String password,
        @NotBlank
        @Schema(description = "Password confirmation; must match password", example = "Str0ngP@ssw0rd!")
        String repeatPassword
) {
    public CreateUserRequest{
        if (!password.equals(repeatPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}
