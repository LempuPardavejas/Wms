package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class BudgetVarianceResponse {
    private UUID id;
    private GLAccountResponse glAccount;
    private LocalDate varianceDate;
    private BigDecimal budgetedAmount;
    private BigDecimal actualAmount;
    private BigDecimal varianceAmount;
    private BigDecimal variancePercentage;
    private String varianceType; // FAVORABLE, UNFAVORABLE, NEUTRAL

    // Dimensions
    private DepartmentResponse department;
    private CostCenterResponse costCenter;
    private BusinessObjectResponse businessObject;

    private String notes;
}
