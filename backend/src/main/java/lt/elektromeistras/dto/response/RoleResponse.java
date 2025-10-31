package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Role response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private String id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
    private Set<PermissionResponse> permissions;
}
