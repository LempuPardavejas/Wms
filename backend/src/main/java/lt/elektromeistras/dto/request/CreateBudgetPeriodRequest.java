package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBudgetPeriodRequest {
    @NotNull
    private String code;

    @NotNull
    private String name;

    private String description;

    @NotNull
    private String periodType; // YEAR, QUARTER, MONTH, CUSTOM

    @NotNull
    private Integer fiscalYear;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
