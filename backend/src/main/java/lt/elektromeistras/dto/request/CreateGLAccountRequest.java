package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGLAccountRequest {
    @NotNull
    private String code;

    @NotNull
    private String name;

    private String description;

    @NotNull
    private String accountType; // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE, COST_OF_SALES

    @NotNull
    private String accountCategory;

    private java.util.UUID parentAccountId;

    @NotNull
    private String normalBalance; // DEBIT, CREDIT

    private Boolean allowDirectPosting = true;
    private Boolean requireDepartment = false;
    private Boolean requireCostCenter = false;
    private Boolean requireBusinessObject = false;

    private Integer sortOrder;
}
