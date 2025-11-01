package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class BudgetPeriodResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String periodType;
    private Integer fiscalYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
