package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_code", columnList = "code"),
        @Index(name = "idx_product_sku", columnList = "sku"),
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_active", columnList = "is_active"),
        @Index(name = "idx_product_active_code", columnList = "is_active,code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(length = 50)
    private String ean;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(name = "manufacturer_id")
    private UUID manufacturerId;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure = "PCS";

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "cost_price", precision = 19, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = new BigDecimal("21.00");

    @Column(name = "is_cable", nullable = false)
    private Boolean isCable = false;

    @Column(name = "is_modular", nullable = false)
    private Boolean isModular = false;

    @Column(name = "module_width", precision = 5, scale = 2)
    private BigDecimal moduleWidth;

    @Column(precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "min_stock_level", precision = 19, scale = 3)
    private BigDecimal minStockLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
