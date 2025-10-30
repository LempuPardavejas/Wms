package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnLineResponse {
    private UUID id;
    private UUID returnId;
    private UUID orderLineId;
    private UUID productId;
    private String productSku;
    private String productName;
    private UUID returnReasonId;
    private String returnReasonName;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReturned;
    private BigDecimal quantityAccepted;
    private BigDecimal quantityRejected;
    private String condition;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal taxRate;
    private BigDecimal lineTotal;
    private BigDecimal refundAmount;
    private Boolean restockEligible;
    private Boolean restocked;
    private LocalDateTime restockedDate;
    private UUID warehouseLocationId;
    private String notes;
    private String inspectionNotes;
}
