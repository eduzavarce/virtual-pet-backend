package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.application.FeedPetService;
import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pets")
@Tag(name = "Pets", description = "Operations related to pets of the authenticated user")
public class FeedPetPostController {
    private final FeedPetService feedPetService;
    private final PetRepository petRepository;

    public FeedPetPostController(FeedPetService feedPetService, PetRepository petRepository) {
        this.feedPetService = feedPetService;
        this.petRepository = petRepository;
    }

    @PostMapping("/{id}/feed")
    @Operation(
            summary = "Feed a pet",
            description = "Feeds the specified pet (owned by the authenticated user), decreasing its hunger by 10 down to a minimum of 0.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(responseCode = "200", description = "Pet fed successfully",
            content = @Content(
                    schema = @Schema(implementation = ResponseDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "FeedPetSuccessExample",
                            value = "{\n  \"status\": \"success\",\n  \"data\": {\n    \"id\": \"a1b2c3d4-e5f6-7890-abcd-ef0123456789\",\n    \"name\": \"Fluffy\",\n    \"health\": 50,\n    \"hunger\": 40,\n    \"stamina\": 50,\n    \"type\": \"CAT\",\n    \"ownerId\": \"3f2a0c83-6c2a-4c3a-a3b3-9f1a2b2c3d4e\",\n    \"ownerUsername\": \"john.doe\"\n  }\n}"
                    )
            ))
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pet not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ResponseDto<PetWithOwnerDto>> feed(@AuthenticationPrincipal UserPostgresEntity principal,
                                                             @PathVariable("id") String petId) {
        String ownerId = principal.getId();
        Pet updated = feedPetService.execute(petId, ownerId);

        var entity = petRepository.findByIdAndOwner_Id(petId, ownerId).orElseThrow();
        PetWithOwnerDto dto = new PetWithOwnerDto(
                entity.getId(),
                updated.getName(),
                entity.getOwner().getId(),
                entity.getOwner().getUsername(),
                updated.getHealth(),
                updated.getHunger(),
                updated.getStamina(),
                updated.getType()
        );
        return ResponseEntity.ok(new ResponseDto<>("success", dto));
    }
}
