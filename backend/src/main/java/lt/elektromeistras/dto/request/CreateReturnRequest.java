package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateReturnRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID warehouseId;

    @NotEmpty
    private List<ReturnLineRequest> lines;

    private String notes;
}
