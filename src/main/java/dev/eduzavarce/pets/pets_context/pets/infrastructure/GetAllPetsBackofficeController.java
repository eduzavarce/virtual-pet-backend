package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.pets_context.pets.application.ListAllPetsService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/backoffice/pets")
@Tag(name = "Backoffice - Pets", description = "Admin operations related to all pets")
public class GetAllPetsBackofficeController {
    private final ListAllPetsService listAllPetsService;

    public GetAllPetsBackofficeController(ListAllPetsService listAllPetsService) {
        this.listAllPetsService = listAllPetsService;
    }

    @GetMapping
    @Operation(
            summary = "List all pets",
            description = "Returns all pets across all users. Admin-only endpoint.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(responseCode = "200", description = "List of all pets returned",
            content = @Content(
                    schema = @Schema(implementation = ResponseDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "ListAllPetsSuccessExample",
                            value = "{\n  \"status\": \"success\",\n  \"data\": [\n    {\n      \"id\": \"a1b2c3d4-e5f6-7890-abcd-ef0123456789\",\n      \"name\": \"Fluffy\",\n      \"health\": 50,\n      \"hunger\": 50,\n      \"stamina\": 50,\n      \"type\": \"CAT\",\n      \"ownerId\": \"3f2a0c83-6c2a-4c3a-a3b3-9f1a2b2c3d4e\",\n      \"ownerUsername\": \"john.doe\"\n    }\n  ]\n}"
                    )
            ))
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ResponseDto<List<PetWithOwnerDto>>> listAll() {
        List<PetWithOwnerDto> pets = listAllPetsService.execute();
        return ResponseEntity.ok(new ResponseDto<>("success", pets));
    }
}
