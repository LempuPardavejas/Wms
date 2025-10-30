package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight DTO for order list display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    private UUID id;
    private String orderNumber;
    private Instant orderDate;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private Integer lineCount;

    /**
     * Display label for order selection: "ORD-20251028-0001 - €302.50 (3 items)"
     */
    public String getLabel() {
        return String.format("%s - €%.2f (%d %s)",
                orderNumber,
                totalAmount,
                lineCount,
                lineCount == 1 ? "item" : "items");
    }
}
