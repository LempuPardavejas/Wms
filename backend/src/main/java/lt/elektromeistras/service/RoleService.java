package lt.elektromeistras.service;

import lombok.RequiredArgsConstructor;
import lt.elektromeistras.domain.Role;
import lt.elektromeistras.dto.response.PermissionResponse;
import lt.elektromeistras.dto.response.RoleResponse;
import lt.elektromeistras.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Role management service
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Get all roles
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active roles
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getActiveRoles() {
        return roleRepository.findByIsActiveTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get role by ID
     */
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaidmuo nerastas"));
        return convertToResponse(role);
    }

    /**
     * Get role by code
     */
    @Transactional(readOnly = true)
    public RoleResponse getRoleByCode(String code) {
        Role role = roleRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Vaidmuo nerastas"));
        return convertToResponse(role);
    }

    /**
     * Convert Role entity to RoleResponse DTO
     */
    private RoleResponse convertToResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
                .map(permission -> PermissionResponse.builder()
                        .id(permission.getId().toString())
                        .code(permission.getCode())
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .category(permission.getCategory())
                        .build())
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId().toString())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isActive(role.getIsActive())
                .permissions(permissionResponses)
                .build();
    }
}
