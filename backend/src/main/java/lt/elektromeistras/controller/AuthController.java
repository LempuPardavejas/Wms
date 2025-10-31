package lt.elektromeistras.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lt.elektromeistras.dto.request.LoginRequest;
import lt.elektromeistras.dto.response.LoginResponse;
import lt.elektromeistras.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate token endpoint
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse.UserResponse> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        LoginResponse.UserResponse user = authService.validateToken(token);
        return ResponseEntity.ok(user);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
