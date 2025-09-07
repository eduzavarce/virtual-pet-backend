package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.application.GetUserPetsService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pets")
@Tag(name = "Pets", description = "Operations related to pets of the authenticated user")
public class GetUserPetsController {
    private final GetUserPetsService getUserPetsService;

    public GetUserPetsController(GetUserPetsService getUserPetsService) {
        this.getUserPetsService = getUserPetsService;
    }

    @GetMapping
    @Operation(
            summary = "Get my pets",
            description = "Returns the list of pets that belong to the currently authenticated user. The user id is resolved from the authenticated principal (JWT).",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(responseCode = "200", description = "List of pets returned",
            content = @Content(
                    schema = @Schema(implementation = ResponseDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "GetMyPetsSuccessExample",
                            value = "{\n  \"status\": \"success\",\n  \"data\": [\n    {\n      \"id\": \"a1b2c3d4-e5f6-7890-abcd-ef0123456789\",\n      \"name\": \"Fluffy\",\n      \"health\": 95,\n      \"hunger\": 10,\n      \"stamina\": 80,\n      \"type\": \"CAT\",\n      \"ownerId\": \"3f2a0c83-6c2a-4c3a-a3b3-9f1a2b2c3d4e\",\n      \"ownerUsername\": \"john.doe\"\n    }\n  ]\n}"
                    )
            ))
    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ResponseDto<List<PetWithOwnerDto>>> getMine(@AuthenticationPrincipal UserPostgresEntity principal) {
        String userId = principal.getId();
        List<PetWithOwnerDto> pets = getUserPetsService.execute(userId);
        ResponseDto<List<PetWithOwnerDto>> response = new ResponseDto<>("success", pets);
        return ResponseEntity.ok(response);
    }
}
