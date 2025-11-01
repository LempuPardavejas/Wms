package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Budget Period - Defines fiscal periods for budgeting
 */
@Entity
@Table(name = "budget_periods", indexes = {
    @Index(name = "idx_budget_period_code", columnList = "code", unique = true),
    @Index(name = "idx_budget_period_dates", columnList = "start_date,end_date"),
    @Index(name = "idx_budget_period_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BudgetPeriod extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PeriodStatus status = PeriodStatus.DRAFT;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum PeriodType {
        YEAR,       // Metinis
        QUARTER,    // Ketvirčio
        MONTH,      // Mėnesio
        CUSTOM      // Pasirinktas laikotarpis
    }

    public enum PeriodStatus {
        DRAFT,      // Juodraštis
        ACTIVE,     // Aktyvus
        CLOSED,     // Uždarytas
        ARCHIVED    // Archyvuotas
    }
}
