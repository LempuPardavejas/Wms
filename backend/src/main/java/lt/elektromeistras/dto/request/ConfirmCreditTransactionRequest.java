package lt.elektromeistras.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to confirm a credit transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmCreditTransactionRequest {

    private String confirmedBy;

    private String signatureData; // Base64 encoded signature image (optional)

    private String notes;
}
