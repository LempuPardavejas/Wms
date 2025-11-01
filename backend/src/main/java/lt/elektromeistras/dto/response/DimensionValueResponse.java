package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class DimensionValueResponse {
    private UUID id;
    private UUID dimensionTypeId;
    private String code;
    private String name;
    private String description;
    private String textValue;
    private Double numericValue;
    private LocalDate dateValue;
    private Boolean booleanValue;
    private UUID parentValueId;
    private Boolean isActive;
}
