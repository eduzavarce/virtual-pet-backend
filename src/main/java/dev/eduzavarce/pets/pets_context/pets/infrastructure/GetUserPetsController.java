package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import dev.eduzavarce.pets.pets_context.pets.application.GetUserPetsService;
import dev.eduzavarce.pets.pets_context.pets.domain.PetWithOwnerDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pets")
public class GetUserPetsController {
    private final GetUserPetsService getUserPetsService;

    public GetUserPetsController(GetUserPetsService getUserPetsService) {
        this.getUserPetsService = getUserPetsService;
    }

    @GetMapping("/user/{userId}")
    public List<PetWithOwnerDto> getByUser(@PathVariable String userId) {
        return getUserPetsService.execute(userId);
    }
}
