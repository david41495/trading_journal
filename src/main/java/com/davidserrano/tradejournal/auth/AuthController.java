package com.davidserrano.tradejournal.auth;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthUserResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthUserResponse user = authService.register(request);
        return ResponseEntity.created(URI.create("/api/auth/me")).body(user);
    }

    @GetMapping("/me")
    public AuthUserResponse currentUser(Principal principal) {
        return authService.toResponse(authService.requireUser(principal.getName()));
    }
}
