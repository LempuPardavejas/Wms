package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for credit transaction line item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionLineResponse {

    private UUID id;
    private UUID productId;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String notes;
}
