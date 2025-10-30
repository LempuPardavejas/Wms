package lt.elektromeistras.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for quick order creation
 * Allows fast entry with just customer ID, product codes and quantities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickOrderRequest {

    private UUID customerId;
    private UUID projectId;
    private List<QuickOrderLineRequest> lines;
    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickOrderLineRequest {
        private String productCode;
        private BigDecimal quantity;
        private String notes;
    }
}
