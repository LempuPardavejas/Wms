package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InspectReturnLineRequest {
    @NotNull
    private UUID returnLineId;

    @NotNull
    private String condition;

    @NotNull
    private BigDecimal quantityAccepted;

    @NotNull
    private BigDecimal quantityRejected;

    private UUID warehouseLocationId;

    private String inspectionNotes;
}
