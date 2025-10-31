package lt.elektromeistras.service;

import lombok.RequiredArgsConstructor;
import lt.elektromeistras.domain.Role;
import lt.elektromeistras.domain.User;
import lt.elektromeistras.dto.request.CreateUserRequest;
import lt.elektromeistras.dto.request.UpdateUserRequest;
import lt.elektromeistras.dto.response.UserResponse;
import lt.elektromeistras.repository.RoleRepository;
import lt.elektromeistras.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User management service
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));
        return convertToResponse(user);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));
        return convertToResponse(user);
    }

    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        return userRepository.searchUsers(query).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create new user
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Vartotojo vardas jau egzistuoja");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El. paštas jau egzistuoja");
        }

        // Get roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoleCodes() != null && !request.getRoleCodes().isEmpty()) {
            for (String roleCode : request.getRoleCodes()) {
                Role role = roleRepository.findByCode(roleCode)
                        .orElseThrow(() -> new RuntimeException("Vaidmuo '" + roleCode + "' nerastas"));
                roles.add(role);
            }
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .isActive(true)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        return convertToResponse(user);
    }

    /**
     * Update user
     */
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));

        // Update fields
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("El. paštas jau egzistuoja");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        // Update roles
        if (request.getRoleCodes() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleCode : request.getRoleCodes()) {
                Role role = roleRepository.findByCode(roleCode)
                        .orElseThrow(() -> new RuntimeException("Vaidmuo '" + roleCode + "' nerastas"));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        user = userRepository.save(user);
        return convertToResponse(user);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));
        userRepository.delete(user);
    }

    /**
     * Activate/deactivate user
     */
    @Transactional
    public UserResponse toggleUserStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));
        user.setIsActive(!user.getIsActive());
        user = userRepository.save(user);
        return convertToResponse(user);
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToResponse(User user) {
        Set<UserResponse.RoleResponse> roleResponses = user.getRoles().stream()
                .map(role -> UserResponse.RoleResponse.builder()
                        .id(role.getId().toString())
                        .code(role.getCode())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .collect(Collectors.toSet());

        Set<String> permissions = user.getAllPermissions().stream()
                .map(permission -> permission.getCode())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleResponses)
                .permissions(permissions)
                .build();
    }
}
