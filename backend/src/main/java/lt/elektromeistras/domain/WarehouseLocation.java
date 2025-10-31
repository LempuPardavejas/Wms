package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * WarehouseLocation entity representing a specific location/bin within a warehouse.
 * Used for precise inventory tracking and stock management.
 */
@Entity
@Table(name = "warehouse_locations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_warehouse_location_code", columnNames = {"warehouse_id", "location_code"})
    },
    indexes = {
        @Index(name = "idx_location_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_location_code", columnList = "location_code")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WarehouseLocation extends BaseEntity {

    /**
     * The warehouse this location belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    /**
     * Unique location code within the warehouse (e.g., "A-01-02", "SHELF-12-BIN-3")
     */
    @Column(name = "location_code", nullable = false, length = 50)
    private String code;

    /**
     * Descriptive name for the location
     */
    @Column(name = "name", length = 255)
    private String name;

    /**
     * Location type (e.g., "SHELF", "BIN", "RACK", "PALLET", "FLOOR")
     */
    @Column(name = "location_type", length = 50)
    private String locationType;

    /**
     * Aisle identifier
     */
    @Column(name = "aisle", length = 20)
    private String aisle;

    /**
     * Row identifier
     */
    @Column(name = "row", length = 20)
    private String row;

    /**
     * Shelf/level identifier
     */
    @Column(name = "level", length = 20)
    private String level;

    /**
     * Bin/position identifier
     */
    @Column(name = "bin", length = 20)
    private String bin;

    /**
     * Maximum capacity (if applicable)
     */
    @Column(name = "capacity", precision = 19, scale = 3)
    private BigDecimal capacity;

    /**
     * Current capacity usage
     */
    @Column(name = "current_quantity", precision = 19, scale = 3)
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    /**
     * Whether this location is currently active and available for use
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Whether this location can be used for picking operations
     */
    @Column(name = "is_pickable", nullable = false)
    private Boolean isPickable = true;

    /**
     * Whether this location is designated for receiving/putaway
     */
    @Column(name = "is_receiving", nullable = false)
    private Boolean isReceiving = false;

    /**
     * Additional notes about the location
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
