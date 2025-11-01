package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Dimension Type - Definition for dynamic dimensions
 * Allows users to create custom analysis dimensions (e.g., Customer Groups, Product Themes, etc.)
 */
@Entity
@Table(name = "dimension_types", indexes = {
    @Index(name = "idx_dimension_type_code", columnList = "code", unique = true),
    @Index(name = "idx_dimension_type_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DimensionType extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    @Builder.Default
    private DataType dataType = DataType.TEXT;

    @Column(name = "is_hierarchical")
    @Builder.Default
    private Boolean isHierarchical = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public enum DataType {
        TEXT,
        NUMERIC,
        DATE,
        BOOLEAN
    }
}
