package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for credit transaction details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionResponse {

    private UUID id;
    private String transactionNumber;
    private UUID customerId;
    private String customerCode;
    private String customerName;
    private String transactionType;
    private String status;
    private List<CreditTransactionLineResponse> lines = new ArrayList<>();
    private BigDecimal totalAmount;
    private Integer totalItems;
    private String performedBy;
    private String performedByRole;
    private String confirmedBy;
    private Instant confirmedAt;
    private String signatureData; // Base64 encoded signature image
    private String photoData; // Base64 encoded photo of person who signed
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
