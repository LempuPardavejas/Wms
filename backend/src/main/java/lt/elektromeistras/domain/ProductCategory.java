package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Product category for organizing products
 */
@Entity
@Table(name = "product_categories", indexes = {
    @Index(name = "idx_category_code", columnList = "code", unique = true),
    @Index(name = "idx_category_name", columnList = "name")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCategory extends BaseEntity {

    /**
     * Category code (unique identifier)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Category name
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Parent category for hierarchical structure
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;

    /**
     * Description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Display order
     */
    @Column
    private Integer displayOrder;

    /**
     * Active status
     */
    @Column(nullable = false)
    private Boolean isActive = true;
}
