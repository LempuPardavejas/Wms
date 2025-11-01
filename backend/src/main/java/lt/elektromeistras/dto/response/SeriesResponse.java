package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SeriesResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String seriesType;
    private String prefix;
    private Long currentNumber;
    private Boolean isActive;
}
