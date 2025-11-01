package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DimensionTypeResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String dataType;
    private Boolean isHierarchical;
    private Boolean isActive;
}
