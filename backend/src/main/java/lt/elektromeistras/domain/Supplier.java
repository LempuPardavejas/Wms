package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Tiekėjas (Supplier) - represents a supplier/vendor in the system
 */
@Entity
@Table(name = "suppliers", indexes = {
    @Index(name = "idx_supplier_code", columnList = "code", unique = true),
    @Index(name = "idx_supplier_name", columnList = "name")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity {

    /**
     * Tiekėjo kodas (unique identifier from supplier system)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Tiekėjo pavadinimas
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Company registration code
     */
    @Column(length = 50)
    private String registrationCode;

    /**
     * VAT code
     */
    @Column(length = 50)
    private String vatCode;

    /**
     * Contact person
     */
    @Column(length = 100)
    private String contactPerson;

    /**
     * Phone number
     */
    @Column(length = 50)
    private String phone;

    /**
     * Email address
     */
    @Column(length = 100)
    private String email;

    /**
     * Address
     */
    @Column(length = 255)
    private String address;

    /**
     * City
     */
    @Column(length = 100)
    private String city;

    /**
     * Postal code
     */
    @Column(length = 20)
    private String postalCode;

    /**
     * Country
     */
    @Column(length = 100)
    private String country;

    /**
     * Payment terms in days
     */
    @Column
    private Integer paymentTermDays;

    /**
     * Notes
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Active status
     */
    @Column(nullable = false)
    private Boolean isActive = true;
}
