package lt.elektromeistras.service;

import lombok.RequiredArgsConstructor;
import lt.elektromeistras.domain.User;
import lt.elektromeistras.dto.request.LoginRequest;
import lt.elektromeistras.dto.response.LoginResponse;
import lt.elektromeistras.repository.UserRepository;
import lt.elektromeistras.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Authentication service
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Login user and return JWT tokens
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            // Get user from database
            User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                    .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            // Build user response
            LoginResponse.UserResponse userResponse = LoginResponse.UserResponse.builder()
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .roles(user.getRoles().stream()
                            .map(role -> role.getCode())
                            .collect(Collectors.toSet()))
                    .permissions(user.getAllPermissions().stream()
                            .map(permission -> permission.getCode())
                            .collect(Collectors.toSet()))
                    .build();

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .user(userResponse)
                    .build();

        } catch (AuthenticationException e) {
            throw new RuntimeException("Neteisingas vartotojo vardas arba slaptažodis");
        }
    }

    /**
     * Validate token and get user info
     */
    @Transactional(readOnly = true)
    public LoginResponse.UserResponse validateToken(String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));

        return LoginResponse.UserResponse.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getCode())
                        .collect(Collectors.toSet()))
                .permissions(user.getAllPermissions().stream()
                        .map(permission -> permission.getCode())
                        .collect(Collectors.toSet()))
                .build();
    }
}
