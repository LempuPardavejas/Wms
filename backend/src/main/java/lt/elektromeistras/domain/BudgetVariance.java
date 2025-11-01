package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Budget Variance - Budget vs Actual comparison
 * Stores calculated variances between budgeted and actual amounts
 */
@Entity
@Table(name = "budget_variances", indexes = {
    @Index(name = "idx_variance_budget", columnList = "budget_id"),
    @Index(name = "idx_variance_account", columnList = "gl_account_id"),
    @Index(name = "idx_variance_period", columnList = "variance_date"),
    @Index(name = "idx_variance_dimensions", columnList = "department_id,cost_center_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BudgetVariance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_line_id")
    private BudgetLine budgetLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private GLAccount glAccount;

    @Column(name = "variance_date", nullable = false)
    private LocalDate varianceDate;

    @Column(name = "budgeted_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal budgetedAmount = BigDecimal.ZERO;

    @Column(name = "actual_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(name = "variance_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal varianceAmount = BigDecimal.ZERO;

    @Column(name = "variance_percentage", precision = 10, scale = 2)
    private BigDecimal variancePercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "variance_type")
    private VarianceType varianceType;

    // Dimensions for variance analysis
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

    @Column(name = "notes", length = 500)
    private String notes;

    public enum VarianceType {
        FAVORABLE,      // Palankus nukrypimas
        UNFAVORABLE,    // Nepalankus nukrypimas
        NEUTRAL         // Neutralus
    }

    /**
     * Calculate variance amount and percentage
     */
    public void calculateVariance() {
        this.varianceAmount = actualAmount.subtract(budgetedAmount);

        if (budgetedAmount.compareTo(BigDecimal.ZERO) != 0) {
            this.variancePercentage = varianceAmount
                    .divide(budgetedAmount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else {
            this.variancePercentage = BigDecimal.ZERO;
        }

        // Determine variance type
        determineVarianceType();
    }

    /**
     * Determine if variance is favorable or unfavorable based on account type
     */
    private void determineVarianceType() {
        if (varianceAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.varianceType = VarianceType.NEUTRAL;
            return;
        }

        GLAccount.AccountType accountType = glAccount.getAccountType();

        // For revenue accounts, higher actual is favorable
        // For expense accounts, lower actual is favorable
        if (accountType == GLAccount.AccountType.REVENUE) {
            this.varianceType = varianceAmount.compareTo(BigDecimal.ZERO) > 0
                    ? VarianceType.FAVORABLE
                    : VarianceType.UNFAVORABLE;
        } else if (accountType == GLAccount.AccountType.EXPENSE ||
                   accountType == GLAccount.AccountType.COST_OF_SALES) {
            this.varianceType = varianceAmount.compareTo(BigDecimal.ZERO) < 0
                    ? VarianceType.FAVORABLE
                    : VarianceType.UNFAVORABLE;
        } else {
            this.varianceType = VarianceType.NEUTRAL;
        }
    }
}
