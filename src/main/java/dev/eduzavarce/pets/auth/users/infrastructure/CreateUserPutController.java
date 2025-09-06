package dev.eduzavarce.pets.auth.users.infrastructure;

import dev.eduzavarce.pets.auth.users.application.CreateUserService;
import dev.eduzavarce.pets.shared.exceptions.ErrorResponse;
import dev.eduzavarce.pets.shared.exceptions.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Operations related to users")
public class CreateUserPutController {
    private final CreateUserService createUserService;

    public CreateUserPutController(CreateUserService createUserService) {
        this.createUserService = createUserService;
    }

    @PutMapping
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user account. The id must be a UUID v4. The password must meet strength requirements.",
            requestBody = @RequestBody(
                    required = true,
                    description = "CreateUserRequest payload",
                    content = @Content(
                            schema = @Schema(implementation = CreateUserRequest.class),
                            examples = @ExampleObject(
                                    name = "CreateUserRequestExample",
                                    value = "{\n  \"id\": \"3f2a0c83-6c2a-4c3a-a3b3-9f1a2b2c3d4e\",\n  \"username\": \"john_doe\",\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"Str0ngP@ssw0rd!\",\n  \"repeatPassword\": \"Str0ngP@ssw0rd!\"\n}"
                            )
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "User with same username or email already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\n  \"message\": \"User already exists\"\n}")))
    @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\n  \"message\": \"Internal server error\"\n}")))
    public ResponseEntity<Void> createUser(@org.springframework.web.bind.annotation.RequestBody @Valid CreateUserRequest createUserRequest) {
        createUserService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
