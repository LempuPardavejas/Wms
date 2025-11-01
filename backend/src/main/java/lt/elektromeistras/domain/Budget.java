package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Budget - Master budget record
 * Represents a budget plan for a specific period with dimensions
 */
@Entity
@Table(name = "budgets", indexes = {
    @Index(name = "idx_budget_code", columnList = "code", unique = true),
    @Index(name = "idx_budget_period", columnList = "budget_period_id"),
    @Index(name = "idx_budget_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Budget extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_period_id", nullable = false)
    private BudgetPeriod budgetPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", nullable = false)
    private BudgetType budgetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BudgetStatus status = BudgetStatus.DRAFT;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BudgetLine> budgetLines = new ArrayList<>();

    @Column(name = "version")
    private Integer version;

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum BudgetType {
        REVENUE,        // Pajamų biudžetas
        EXPENSE,        // Išlaidų biudžetas
        CAPITAL,        // Kapitalo biudžetas
        CASH_FLOW,      // Pinigų srautų biudžetas
        COMPREHENSIVE   // Bendras biudžetas
    }

    public enum BudgetStatus {
        DRAFT,          // Juodraštis
        SUBMITTED,      // Pateiktas
        APPROVED,       // Patvirtintas
        REJECTED,       // Atmestas
        ACTIVE,         // Aktyvus
        COMPLETED,      // Užbaigtas
        CANCELLED       // Atšauktas
    }

    public void addBudgetLine(BudgetLine budgetLine) {
        budgetLines.add(budgetLine);
        budgetLine.setBudget(this);
        recalculateTotalAmount();
    }

    public void removeBudgetLine(BudgetLine budgetLine) {
        budgetLines.remove(budgetLine);
        budgetLine.setBudget(null);
        recalculateTotalAmount();
    }

    public void recalculateTotalAmount() {
        this.totalAmount = budgetLines.stream()
                .map(BudgetLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
