package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.pets_context.pets.application.AdminDeletePetService;
import dev.eduzavarce.pets.shared.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backoffice/pets")
@Tag(name = "Backoffice - Pets", description = "Admin operations related to all pets")
public class DeletePetBackofficeController {
    private final AdminDeletePetService adminDeletePetService;

    public DeletePetBackofficeController(AdminDeletePetService adminDeletePetService) {
        this.adminDeletePetService = adminDeletePetService;
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete any pet by id",
            description = "Deletes the specified pet by id. Admin-only endpoint.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponse(responseCode = "204", description = "Pet deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pet not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable("id") String petId) {
        adminDeletePetService.execute(petId);
        return ResponseEntity.noContent().build();
    }
}
