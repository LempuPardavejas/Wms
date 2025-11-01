package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Cost Center (Centras) - Static dimension
 * Represents cost/profit centers for financial tracking
 */
@Entity
@Table(name = "cost_centers", indexes = {
    @Index(name = "idx_cost_center_code", columnList = "code", unique = true),
    @Index(name = "idx_cost_center_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CostCenter extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "center_type", nullable = false)
    private CenterType centerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public enum CenterType {
        COST_CENTER,      // Išlaidų centras
        PROFIT_CENTER,    // Pelno centras
        INVESTMENT_CENTER // Investicijų centras
    }
}
