package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BusinessObjectResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String objectType;
    private UUID departmentId;
    private Boolean isActive;
}
