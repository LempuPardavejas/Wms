package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BudgetResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private BudgetPeriodResponse budgetPeriod;
    private String budgetType;
    private String status;
    private BigDecimal totalAmount;
    private Integer version;
    private String notes;
    private LocalDateTime approvedAt;
    private List<BudgetLineResponse> budgetLines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
