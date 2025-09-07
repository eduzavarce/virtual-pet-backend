package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.application.RenamePetService;
import dev.eduzavarce.pets.shared.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pets")
@Tag(name = "Pets", description = "Operations related to pets of the authenticated user")
public class RenamePetPatchController {
    private final RenamePetService renamePetService;

    public RenamePetPatchController(RenamePetService renamePetService) {
        this.renamePetService = renamePetService;
    }

    @PatchMapping("/{id}/name")
    @Operation(
            summary = "Rename a pet",
            description = "Renames the specified pet if it belongs to the authenticated user.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(responseCode = "204", description = "Pet renamed successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pet not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> rename(@AuthenticationPrincipal UserPostgresEntity principal,
                                       @PathVariable("id") String petId,
                                       @RequestBody RenamePetRequest request) {
        String ownerId = principal.getId();
        renamePetService.execute(petId, ownerId, request.name());
        return ResponseEntity.noContent().build();
    }

    public record RenamePetRequest(
            @Schema(example = "Mr. Whiskers") String name
    ) {
    }
}
