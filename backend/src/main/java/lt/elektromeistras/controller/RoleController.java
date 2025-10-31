package lt.elektromeistras.controller;

import lombok.RequiredArgsConstructor;
import lt.elektromeistras.dto.response.RoleResponse;
import lt.elektromeistras.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Role management REST controller
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Get all roles
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN_FULL')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Get active roles
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ADMIN_FULL')")
    public ResponseEntity<List<RoleResponse>> getActiveRoles() {
        List<RoleResponse> roles = roleService.getActiveRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_FULL')")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable UUID id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Get role by code
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('ADMIN_FULL')")
    public ResponseEntity<RoleResponse> getRoleByCode(@PathVariable String code) {
        RoleResponse role = roleService.getRoleByCode(code);
        return ResponseEntity.ok(role);
    }
}
