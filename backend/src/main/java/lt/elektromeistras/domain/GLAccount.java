package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * GL Account - Chart of Accounts (Sąskaitų planas)
 * Represents accounts in the General Ledger
 */
@Entity
@Table(name = "gl_accounts", indexes = {
    @Index(name = "idx_gl_account_code", columnList = "code", unique = true),
    @Index(name = "idx_gl_account_type", columnList = "account_type"),
    @Index(name = "idx_gl_account_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GLAccount extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_category", nullable = false)
    private AccountCategory accountCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    private GLAccount parentAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "normal_balance", nullable = false)
    private NormalBalance normalBalance;

    @Column(name = "allow_direct_posting")
    @Builder.Default
    private Boolean allowDirectPosting = true;

    @Column(name = "require_department")
    @Builder.Default
    private Boolean requireDepartment = false;

    @Column(name = "require_cost_center")
    @Builder.Default
    private Boolean requireCostCenter = false;

    @Column(name = "require_business_object")
    @Builder.Default
    private Boolean requireBusinessObject = false;

    @Column(name = "current_balance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public enum AccountType {
        ASSET,              // Turtas
        LIABILITY,          // Įsipareigojimai
        EQUITY,             // Nuosavas kapitalas
        REVENUE,            // Pajamos
        EXPENSE,            // Išlaidos
        COST_OF_SALES       // Savikaina
    }

    public enum AccountCategory {
        CURRENT_ASSET,      // Trumpalaikis turtas
        FIXED_ASSET,        // Ilgalaikis turtas
        CURRENT_LIABILITY,  // Trumpalaikiai įsipareigojimai
        LONG_TERM_LIABILITY,// Ilgalaikiai įsipareigojimai
        EQUITY,             // Nuosavas kapitalas
        OPERATING_REVENUE,  // Veiklos pajamos
        OTHER_REVENUE,      // Kitos pajamos
        OPERATING_EXPENSE,  // Veiklos sąnaudos
        FINANCIAL_EXPENSE,  // Finansinės sąnaudos
        OTHER_EXPENSE       // Kitos sąnaudos
    }

    public enum NormalBalance {
        DEBIT,              // Debetas
        CREDIT              // Kreditas
    }
}
