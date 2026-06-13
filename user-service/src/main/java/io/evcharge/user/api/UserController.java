package io.evcharge.user.api;

import io.evcharge.user.api.dto.*;
import io.evcharge.user.service.AuthService;
import io.evcharge.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Registration, login, and user lookup")
public class UserController {

    private final AuthService authService;
    private final UserService  userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PreAuthorize("hasAnyRole('USER','OPERATOR','ADMIN')")
    @GetMapping("/me")
    public UserResponse me(@RequestHeader("X-User-Id") Long userId) {
        return userService.findById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }
}
