package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Journal Entry - Didžiosios knygos žurnalo įrašas
 * Represents a journal entry with header information
 */
@Entity
@Table(name = "journal_entries", indexes = {
    @Index(name = "idx_journal_entry_number", columnList = "entry_number", unique = true),
    @Index(name = "idx_journal_entry_date", columnList = "entry_date"),
    @Index(name = "idx_journal_entry_status", columnList = "status"),
    @Index(name = "idx_journal_entry_type", columnList = "entry_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalEntry extends BaseEntity {

    @Column(name = "entry_number", nullable = false, unique = true, length = 50)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "posting_date")
    private LocalDate postingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private SourceType sourceType;

    @Column(name = "source_document_id")
    private java.util.UUID sourceDocumentId;

    @Column(name = "source_document_number", length = 100)
    private String sourceDocumentNumber;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "total_debit", precision = 19, scale = 2)
    private BigDecimal totalDebit;

    @Column(name = "total_credit", precision = 19, scale = 2)
    private BigDecimal totalCredit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntryStatus status = EntryStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_id")
    private User postedBy;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_period_id")
    private BudgetPeriod budgetPeriod;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JournalEntryLine> journalEntryLines = new ArrayList<>();

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum EntryType {
        MANUAL,             // Rankinis įrašas
        AUTOMATIC,          // Automatinis įrašas
        ADJUSTMENT,         // Koregavimas
        CLOSING,            // Uždarymo įrašas
        OPENING,            // Atidarymo įrašas
        REVERSAL            // Atšaukimo įrašas
    }

    public enum SourceType {
        ORDER,              // Užsakymas
        INVOICE,            // Sąskaita faktūra
        PAYMENT,            // Mokėjimas
        RETURN,             // Grąžinimas
        CREDIT_TRANSACTION, // Kredito operacija
        INVENTORY,          // Inventorizacija
        MANUAL              // Rankinis
    }

    public enum EntryStatus {
        DRAFT,              // Juodraštis
        VALIDATED,          // Patikrintas
        POSTED,             // Užregistruotas
        REVERSED,           // Atšauktas
        DELETED             // Pašalintas
    }

    public void addJournalEntryLine(JournalEntryLine line) {
        journalEntryLines.add(line);
        line.setJournalEntry(this);
        recalculateTotals();
    }

    public void removeJournalEntryLine(JournalEntryLine line) {
        journalEntryLines.remove(line);
        line.setJournalEntry(null);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.totalDebit = journalEntryLines.stream()
                .map(JournalEntryLine::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalCredit = journalEntryLines.stream()
                .map(JournalEntryLine::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isBalanced() {
        return totalDebit != null && totalCredit != null &&
               totalDebit.compareTo(totalCredit) == 0;
    }
}
