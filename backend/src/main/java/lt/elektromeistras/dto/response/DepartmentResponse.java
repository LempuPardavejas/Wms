package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DepartmentResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private UUID parentDepartmentId;
    private Boolean isActive;
}
