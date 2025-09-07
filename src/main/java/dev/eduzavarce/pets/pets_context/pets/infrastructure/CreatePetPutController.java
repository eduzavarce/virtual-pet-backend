package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.application.CreatePetService;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
import dev.eduzavarce.pets.shared.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pets")
@Tag(name = "Pets", description = "Operations related to pets of the authenticated user")
public class CreatePetPutController {
    private final CreatePetService createPetService;

    public CreatePetPutController(CreatePetService createPetService) {
        this.createPetService = createPetService;
    }

    @PutMapping
    @Operation(
            summary = "Create a pet",
            description = "Creates a new pet for the authenticated user. Health, hunger and stamina start at 50.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(responseCode = "201", description = "Pet created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> create(@AuthenticationPrincipal UserPostgresEntity principal,
                                       @RequestBody CreatePetRequest request) {
        String ownerId = principal.getId();
        createPetService.execute(request.id(), request.name(), ownerId, request.type());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public record CreatePetRequest(
            @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789") String id,
            @Schema(example = "Fluffy") String name,
            @Schema(description = "Pet type", example = "CAT", allowableValues = {"CAT", "RABBIT", "DOG", "CANARY"}) PetType type
    ) {
    }
}
