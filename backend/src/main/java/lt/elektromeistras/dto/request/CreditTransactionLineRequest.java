package lt.elektromeistras.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request for a single credit transaction line item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionLineRequest {

    private UUID productId;

    private String productCode;

    private BigDecimal quantity;

    private String notes;
}
