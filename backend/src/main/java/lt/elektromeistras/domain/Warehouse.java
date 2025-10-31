package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Warehouse entity representing a physical warehouse location.
 * Used for inventory management and order fulfillment.
 */
@Entity
@Table(name = "warehouses", uniqueConstraints = {
    @UniqueConstraint(name = "uk_warehouse_code", columnNames = "code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Warehouse extends BaseEntity {

    /**
     * Unique warehouse code (e.g., "WH-001", "MAIN")
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Human-readable warehouse name
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Physical address of the warehouse
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * City where the warehouse is located
     */
    @Column(name = "city", length = 100)
    private String city;

    /**
     * Postal code
     */
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    /**
     * Country
     */
    @Column(name = "country", length = 100)
    private String country;

    /**
     * Phone number of the warehouse
     */
    @Column(name = "phone", length = 50)
    private String phone;

    /**
     * Email contact for the warehouse
     */
    @Column(name = "email", length = 255)
    private String email;

    /**
     * Whether this warehouse is currently active and operational
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Additional notes about the warehouse
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
