package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ReturnLineRequest {
    @NotNull
    private UUID orderLineId;

    @NotNull
    private UUID productId;

    @NotNull
    private UUID returnReasonId;

    @NotNull
    @Positive
    private BigDecimal quantityReturned;

    private String notes;
}
