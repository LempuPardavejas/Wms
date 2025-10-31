package lt.elektromeistras.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Request to create a new credit transaction (pickup or return)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCreditTransactionRequest {

    private UUID customerId;

    private String transactionType; // PICKUP or RETURN

    private List<CreditTransactionLineRequest> lines = new ArrayList<>();

    private String performedBy;

    private String performedByRole; // CUSTOMER, EMPLOYEE, ADMINISTRATOR

    private String notes;
}
