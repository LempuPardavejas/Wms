package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Journal Entry Line - Didžiosios knygos žurnalo eilutė
 * Represents individual line items in a journal entry with all dimensions
 */
@Entity
@Table(name = "journal_entry_lines", indexes = {
    @Index(name = "idx_journal_line_entry", columnList = "journal_entry_id"),
    @Index(name = "idx_journal_line_account", columnList = "gl_account_id"),
    @Index(name = "idx_journal_line_dimensions", columnList = "department_id,cost_center_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalEntryLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private GLAccount glAccount;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "debit_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    // Static Dimensions - required for budget comparison
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_object_id")
    private BusinessObject businessObject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    // Dynamic Dimensions (up to 15)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_1_id")
    private DimensionValue dimension1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_2_id")
    private DimensionValue dimension2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_3_id")
    private DimensionValue dimension3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_4_id")
    private DimensionValue dimension4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_5_id")
    private DimensionValue dimension5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_6_id")
    private DimensionValue dimension6;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_7_id")
    private DimensionValue dimension7;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_8_id")
    private DimensionValue dimension8;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_9_id")
    private DimensionValue dimension9;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_10_id")
    private DimensionValue dimension10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_11_id")
    private DimensionValue dimension11;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_12_id")
    private DimensionValue dimension12;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_13_id")
    private DimensionValue dimension13;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_14_id")
    private DimensionValue dimension14;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_15_id")
    private DimensionValue dimension15;

    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Get the net amount (debit - credit)
     */
    public BigDecimal getNetAmount() {
        return debitAmount.subtract(creditAmount);
    }

    /**
     * Check if this is a debit entry
     */
    public boolean isDebit() {
        return debitAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if this is a credit entry
     */
    public boolean isCredit() {
        return creditAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}
