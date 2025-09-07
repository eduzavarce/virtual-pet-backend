package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.application.CreatePetService;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
import dev.eduzavarce.pets.pets_context.pets.domain.PetWithOwnerDto;
import dev.eduzavarce.pets.shared.core.domain.ResponseDto;
import dev.eduzavarce.pets.shared.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final PetRepository petRepository;

    public CreatePetPutController(CreatePetService createPetService, PetRepository petRepository) {
        this.createPetService = createPetService;
        this.petRepository = petRepository;
    }

    @PutMapping
    @Operation(
            summary = "Create a pet",
            description = "Creates a new pet for the authenticated user. Health, hunger and stamina start at 50.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(responseCode = "200", description = "Pet created successfully",
            content = @Content(
                    schema = @Schema(implementation = ResponseDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "CreatePetSuccessExample",
                            value = "{\n  \"status\": \"success\",\n  \"data\": {\n    \"id\": \"a1b2c3d4-e5f6-7890-abcd-ef0123456789\",\n    \"name\": \"Fluffy\",\n    \"health\": 50,\n    \"hunger\": 50,\n    \"stamina\": 50,\n    \"type\": \"CAT\",\n    \"ownerId\": \"3f2a0c83-6c2a-4c3a-a3b3-9f1a2b2c3d4e\",\n    \"ownerUsername\": \"john.doe\"\n  }\n}"
                    )
            ))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ResponseDto<PetWithOwnerDto>> create(@AuthenticationPrincipal UserPostgresEntity principal,
                                       @RequestBody CreatePetRequest request) {
        String ownerId = principal.getId();
        var pet = createPetService.execute(request.id(), request.name(), ownerId, request.type());
        // fetch owner username from repository to build response consistently
        var entity = petRepository.findByIdAndOwner_Id(pet.getId(), ownerId).orElseThrow();
        PetWithOwnerDto dto = new PetWithOwnerDto(
                entity.getId(),
                pet.getName(),
                entity.getOwner().getId(),
                entity.getOwner().getUsername(),
                pet.getHealth(),
                pet.getHunger(),
                pet.getStamina(),
                pet.getType()
        );
        return ResponseEntity.ok(new ResponseDto<>("success", dto));
    }

    public record CreatePetRequest(
            @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789") String id,
            @Schema(example = "Fluffy") String name,
            @Schema(description = "Pet type", example = "CAT", allowableValues = {"CAT", "RABBIT", "DOG", "CANARY"}) PetType type
    ) {
    }
}
