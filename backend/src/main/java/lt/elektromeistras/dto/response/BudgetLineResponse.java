package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BudgetLineResponse {
    private UUID id;
    private Integer lineNumber;
    private GLAccountResponse glAccount;
    private String description;
    private BigDecimal amount;

    // Static dimensions
    private DepartmentResponse department;
    private BusinessObjectResponse businessObject;
    private CostCenterResponse costCenter;
    private SeriesResponse series;
    private PersonResponse person;

    // Dynamic dimensions
    private DimensionValueResponse dimension1;
    private DimensionValueResponse dimension2;
    private DimensionValueResponse dimension3;
    private DimensionValueResponse dimension4;
    private DimensionValueResponse dimension5;

    private String notes;
}
