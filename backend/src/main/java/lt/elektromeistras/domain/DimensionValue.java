package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Dimension Value - Actual values for dynamic dimensions
 * Represents individual dimension values (e.g., "VIP Customers" for "Customer Groups" dimension)
 */
@Entity
@Table(name = "dimension_values", indexes = {
    @Index(name = "idx_dimension_value_type", columnList = "dimension_type_id"),
    @Index(name = "idx_dimension_value_code", columnList = "dimension_type_id,code", unique = true),
    @Index(name = "idx_dimension_value_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DimensionValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_type_id", nullable = false)
    private DimensionType dimensionType;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "text_value", length = 500)
    private String textValue;

    @Column(name = "numeric_value")
    private Double numericValue;

    @Column(name = "date_value")
    private java.time.LocalDate dateValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_value_id")
    private DimensionValue parentValue;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
