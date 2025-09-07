package dev.eduzavarce.pets.auth.users.infrastructure;

import dev.eduzavarce.pets.auth.users.application.LoginUserService;
import dev.eduzavarce.pets.shared.core.domain.ResponseDto;
import dev.eduzavarce.pets.shared.exceptions.ErrorResponse;
import dev.eduzavarce.pets.shared.exceptions.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/login")
@Tag(name = "Users", description = "Operations related to users")
public class LoginUserPostController {
    private static final Logger log = LoggerFactory.getLogger(LoginUserPostController.class);

    private final LoginUserService service;

    public LoginUserPostController(LoginUserService service) {
        this.service = service;

    }

    @PostMapping
    @Operation(
            summary = "Login user",
            description = "Authenticates a user with email/username and password. Returns a JWT token on success.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Login request payload",
                    content = @Content(
                            schema = @Schema(implementation = LoginUserRequest.class),
                            examples = @ExampleObject(
                                    name = "LoginRequestExample",
                                    value = "{\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"Str0ngP@ssw0rd!\"\n}"
                            )
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(
                    schema = @Schema(implementation = ResponseDto.class),
                    examples = @ExampleObject(
                            name = "LoginSuccessExample",
                            value = "{\n  \"status\": \"success\",\n  \"data\": {\n    \"token\": \"eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwidXNlcklkIjoiM2YyYTBjODMtNmMyYS00YzNhLWEzYjMtOWYxYTJiMmMzZDRlIiwic3ViIjoiam9obi5kb2VAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTcyMzY3MzQsImV4cCI6MTc1NzI0MDMzNH0.e7gyM_tmXL2s90qgP0M_4fNhh9GrpQ3XvrLNur9sBu4\"\n  }\n}"
                    )
            ))
    @ApiResponse(responseCode = "400", description = "Invalid request payload",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class),
                    examples = @ExampleObject(value = "{\n  \"message\": \"Validation failed\",\n  \"errors\": [ { \"field\": \"email\", \"message\": \"must be a well-formed email address\" } ]\n}")))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\n  \"message\": \"Invalid username or password\"\n}")))
    @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\n  \"message\": \"Internal server error\"\n}")))
    public ResponseEntity<ResponseDto<LoginResponse>> execute(@RequestBody @Valid LoginUserRequest request) {
        log.info("[HTTP] POST /api/v1/users/login - login attempt for subject={}", request.email());
        String token = service.login(request.email(), request.password());
        log.info("[HTTP] POST /api/v1/users/login - login successful for subject={}", request.email());
        return ResponseEntity.ok(new ResponseDto<>("success", new LoginResponse(token)));
    }
}
