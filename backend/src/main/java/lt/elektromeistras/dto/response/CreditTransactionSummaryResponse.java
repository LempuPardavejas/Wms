package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight summary response for credit transaction lists
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionSummaryResponse {

    private UUID id;
    private String transactionNumber;
    private String customerCode;
    private String customerName;
    private String transactionType;
    private String status;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private String performedBy;
    private Instant createdAt;
}
