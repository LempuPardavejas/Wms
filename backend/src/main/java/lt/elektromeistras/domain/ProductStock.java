package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ProductStock entity representing inventory levels for a product at a specific warehouse location.
 * Tracks quantity, reservations, and cable-specific information.
 */
@Entity
@Table(name = "product_stock",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_stock_product_warehouse_location",
                columnNames = {"product_id", "warehouse_id", "location_id"})
    },
    indexes = {
        @Index(name = "idx_stock_product", columnList = "product_id"),
        @Index(name = "idx_stock_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_stock_location", columnList = "location_id"),
        @Index(name = "idx_stock_product_warehouse", columnList = "product_id,warehouse_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductStock extends BaseEntity {

    /**
     * The product this stock record is for
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * The warehouse where this stock is located
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    /**
     * Specific location within the warehouse (optional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private WarehouseLocation location;

    /**
     * Total quantity in stock
     */
    @Column(name = "quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    /**
     * Quantity reserved for orders (not yet picked)
     */
    @Column(name = "reserved_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    // Cable-specific fields

    /**
     * Roll ID for cable products (unique identifier for the cable roll)
     */
    @Column(name = "roll_id")
    private UUID rollId;

    /**
     * Roll number for cable products (human-readable identifier like "ROLL-001")
     */
    @Column(name = "roll_number", length = 50)
    private String rollNumber;

    /**
     * Original length of the cable roll when received
     */
    @Column(name = "roll_original_length", precision = 19, scale = 3)
    private BigDecimal rollOriginalLength;

    /**
     * Current remaining length of the cable roll
     */
    @Column(name = "roll_current_length", precision = 19, scale = 3)
    private BigDecimal rollCurrentLength;

    /**
     * Last physical count date
     */
    @Column(name = "last_counted_date")
    private LocalDateTime lastCountedDate;

    /**
     * Quantity at last physical count
     */
    @Column(name = "last_counted_quantity", precision = 19, scale = 3)
    private BigDecimal lastCountedQuantity;

    /**
     * Calculated available quantity (quantity - reserved)
     * This is a transient computed field, not stored in database
     */
    public BigDecimal getAvailableQuantity() {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        if (reservedQuantity == null) {
            return quantity;
        }
        return quantity.subtract(reservedQuantity);
    }

    /**
     * Check if this stock record is for a cable product
     */
    public boolean isCableStock() {
        return rollId != null && rollCurrentLength != null;
    }
}
