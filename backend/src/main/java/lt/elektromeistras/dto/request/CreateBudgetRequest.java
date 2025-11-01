package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateBudgetRequest {
    @NotNull
    private String code;

    @NotNull
    private String name;

    private String description;

    @NotNull
    private UUID budgetPeriodId;

    @NotNull
    private String budgetType; // REVENUE, EXPENSE, CAPITAL, CASH_FLOW, COMPREHENSIVE

    @NotEmpty
    private List<BudgetLineRequest> lines;

    private String notes;
}
