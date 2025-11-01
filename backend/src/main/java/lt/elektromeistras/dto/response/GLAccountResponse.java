package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class GLAccountResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String accountType;
    private String accountCategory;
    private UUID parentAccountId;
    private String normalBalance;
    private Boolean allowDirectPosting;
    private Boolean requireDepartment;
    private Boolean requireCostCenter;
    private Boolean requireBusinessObject;
    private BigDecimal currentBalance;
    private Boolean isActive;
}
