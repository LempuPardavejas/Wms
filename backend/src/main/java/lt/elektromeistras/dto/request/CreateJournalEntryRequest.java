package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateJournalEntryRequest {
    @NotNull
    private String entryNumber;

    @NotNull
    private LocalDate entryDate;

    @NotNull
    private String entryType; // MANUAL, AUTOMATIC, ADJUSTMENT, CLOSING, OPENING, REVERSAL

    private String sourceType; // ORDER, INVOICE, PAYMENT, RETURN, CREDIT_TRANSACTION, INVENTORY, MANUAL

    private UUID sourceDocumentId;

    private String sourceDocumentNumber;

    private String description;

    private UUID budgetPeriodId;

    @NotEmpty
    private List<JournalEntryLineRequest> lines;

    private String notes;
}
