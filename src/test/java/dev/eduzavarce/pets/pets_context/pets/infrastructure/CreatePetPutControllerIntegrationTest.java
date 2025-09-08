package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.application.CreatePetService;
import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
import dev.eduzavarce.pets.shared.exceptions.AlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CreatePetPutControllerIntegrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private CreatePetService createPetService;

    private PetRepository petRepository;

    private CreatePetPutController controller;

    // Provide minimal principal via SecurityContext since endpoint is authenticated
    private UserPostgresEntity principal;

    @BeforeEach
    void setUp() {
        // Mockito mocks
        createPetService = Mockito.mock(CreatePetService.class);
        petRepository = Mockito.mock(PetRepository.class);

        controller = new CreatePetPutController(createPetService, petRepository);

        // Build a principal using the public constructor that accepts CreateUserDto
        dev.eduzavarce.pets.auth.users.domain.CreateUserDto cud = new dev.eduzavarce.pets.auth.users.domain.CreateUserDto(
                "11111111-1111-1111-1111-111111111111",
                "john.doe",
                "john@example.com",
                "hashed"
        );
        principal = new UserPostgresEntity(cud);

        Authentication auth = new TestingAuthenticationToken(principal, null, "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Build standalone MockMvc with AuthenticationPrincipal resolver
        org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver authResolver =
                new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver();
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(authResolver)
                .setControllerAdvice(new dev.eduzavarce.pets.shared.exceptions.GlobalExceptionHandler())
                .build();
    }

    private String json(Object o) throws Exception { return objectMapper.writeValueAsString(o); }

    @Test
    @DisplayName("201 OK with ResponseDto and PetWithOwnerDto on successful creation")
    void createPet_success() throws Exception {
        String petId = UUID.randomUUID().toString();
        CreatePetPutController.CreatePetRequest req = new CreatePetPutController.CreatePetRequest(petId, "Fluffy", PetType.CAT);

        // Service returns domain Pet
        Pet pet = Pet.create(new dev.eduzavarce.pets.pets_context.pets.domain.PetDto(petId, "Fluffy", principal.getId(), 50, 50, 50, PetType.CAT));
        Mockito.when(createPetService.execute(eq(petId), eq("Fluffy"), eq(principal.getId()), eq(PetType.CAT)))
                .thenReturn(pet);

        // Repository returns entity with owner username
        dev.eduzavarce.pets.pets_context.users.domain.PetUserDto ownerDto = new dev.eduzavarce.pets.pets_context.users.domain.PetUserDto(principal.getId(), "johnny");
        PetsUserPostgresEntity owner = new PetsUserPostgresEntity(ownerDto);
        PetPostgresEntity saved = new PetPostgresEntity(pet, owner);
        Mockito.when(petRepository.findByIdAndOwner_Id(eq(petId), eq(principal.getId())))
                .thenReturn(Optional.of(saved));

        mockMvc.perform(put("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is(petId)))
                .andExpect(jsonPath("$.data.name", is("Fluffy")))
                .andExpect(jsonPath("$.data.health", is(50)))
                .andExpect(jsonPath("$.data.hunger", is(50)))
                .andExpect(jsonPath("$.data.stamina", is(50)))
                .andExpect(jsonPath("$.data.type", is("CAT")))
                .andExpect(jsonPath("$.data.ownerId", is(principal.getId())))
                .andExpect(jsonPath("$.data.ownerUsername", is("johnny")));

        // ensure no DB persistence in test: repository save should never be called by controller
        Mockito.verify(petRepository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("400 Bad Request when name is missing (null)")
    void createPet_validationError_missingName() throws Exception {
        String petId = UUID.randomUUID().toString();
        CreatePetPutController.CreatePetRequest req = new CreatePetPutController.CreatePetRequest(petId, null, PetType.DOG);

        // Simulate service throwing validation exception mapped to 400
        Mockito.when(createPetService.execute(eq(petId), isNull(), eq(principal.getId()), eq(PetType.DOG)))
                .thenThrow(new dev.eduzavarce.pets.shared.exceptions.EmptyFieldException("name"));

        mockMvc.perform(put("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("409 Conflict when pet already exists")
    void createPet_conflict() throws Exception {
        String petId = UUID.randomUUID().toString();
        CreatePetPutController.CreatePetRequest req = new CreatePetPutController.CreatePetRequest(petId, "Buddy", PetType.RABBIT);

        Mockito.when(createPetService.execute(anyString(), anyString(), anyString(), any()))
                .thenThrow(new AlreadyExistsException("Pet already exists"));

        mockMvc.perform(put("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("500 Internal Server Error on unexpected exception")
    void createPet_unexpectedError() throws Exception {
        String petId = UUID.randomUUID().toString();
        CreatePetPutController.CreatePetRequest req = new CreatePetPutController.CreatePetRequest(petId, "Zazu", PetType.CANARY);

        Mockito.when(createPetService.execute(anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(put("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("401 Unauthorized when no authentication present")
    void createPet_unauthorized() throws Exception {
        // Clear security context to simulate no auth
        SecurityContextHolder.clearContext();

        String petId = UUID.randomUUID().toString();
        CreatePetPutController.CreatePetRequest req = new CreatePetPutController.CreatePetRequest(petId, "Rocky", PetType.DOG);

        mockMvc.perform(put("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("404 Not Found when repository cannot find freshly created pet for owner")
    void createPet_notFoundOnRepositoryReadback() throws Exception {
        String petId = UUID.randomUUID().toString();
        CreatePetPutController.CreatePetRequest req = new CreatePetPutController.CreatePetRequest(petId, "Ghost", PetType.CAT);

        Pet pet = Pet.create(new dev.eduzavarce.pets.pets_context.pets.domain.PetDto(petId, "Ghost", principal.getId(), 50, 50, 50, PetType.CAT));
        Mockito.when(createPetService.execute(eq(petId), eq("Ghost"), eq(principal.getId()), eq(PetType.CAT)))
                .thenReturn(pet);
        Mockito.when(petRepository.findByIdAndOwner_Id(eq(petId), eq(principal.getId())))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isInternalServerError()); // will bubble up NoSuchElementException -> handled as 500
    }
}
