package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class JournalEntryResponse {
    private UUID id;
    private String entryNumber;
    private LocalDate entryDate;
    private LocalDate postingDate;
    private String entryType;
    private String sourceType;
    private UUID sourceDocumentId;
    private String sourceDocumentNumber;
    private String description;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private String status;
    private BudgetPeriodResponse budgetPeriod;
    private List<JournalEntryLineResponse> journalEntryLines;
    private String notes;
    private LocalDateTime postedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
