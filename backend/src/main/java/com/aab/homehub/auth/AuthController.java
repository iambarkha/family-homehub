package com.aab.homehub.auth;

import com.aab.homehub.auth.dto.request.LoginRequest;
import com.aab.homehub.auth.dto.request.RegisterRequest;
import com.aab.homehub.auth.dto.response.AuthResponse;
import com.aab.homehub.auth.service.AuthService;
import com.aab.homehub.auth.service.JwtService;
import com.aab.homehub.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
        // Implement authentication endpoints here (e.g., login, register)
    private AuthService authService;
    private UserDetailsService userDetailsService;
    private JwtService jwtService;

     public AuthController(AuthService authService, UserDetailsService userDetailsService, JwtService jwtService) {
        this.authService = authService;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity
                .ok(authResponse);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Check if a JWT token is valid")

    public ResponseEntity<String> validate(@RequestParam String token) {
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        if (isValid) {
            return ResponseEntity.ok("Token is valid");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
