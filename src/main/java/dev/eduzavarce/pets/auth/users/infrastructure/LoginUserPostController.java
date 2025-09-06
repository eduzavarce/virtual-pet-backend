package dev.eduzavarce.pets.auth.users.infrastructure;

import dev.eduzavarce.pets.auth.users.application.LoginUserService;
import dev.eduzavarce.pets.shared.core.domain.ResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/login")
public class LoginUserPostController {
    private final LoginUserService service;

    public LoginUserPostController(LoginUserService service) {
        this.service = service;

    }

    @PostMapping
    public ResponseEntity<ResponseDto<LoginResponse>> execute(@RequestBody @Valid LoginUserRequest request) {
        String token = service.login(request.email(), request.password());
        return ResponseEntity.ok(new ResponseDto<>("success", new LoginResponse(token)));
    }
}
