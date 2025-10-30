package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_code", columnList = "code"),
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_company_name", columnList = "company_name"),
        @Index(name = "idx_customer_last_name", columnList = "last_name"),
        @Index(name = "idx_customer_active", columnList = "is_active"),
        @Index(name = "idx_customer_active_type", columnList = "is_active,customer_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    private CustomerType customerType = CustomerType.RETAIL;

    // Business fields
    @Column(name = "company_name", length = 500)
    private String companyName;

    @Column(name = "vat_code", length = 50)
    private String vatCode;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    // Individual fields
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    // Contact
    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 50)
    private String mobile;

    // Address
    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String country = "Lietuva";

    // Business
    @Column(name = "price_group_id")
    private UUID priceGroupId;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "payment_terms_days", nullable = false)
    private Integer paymentTermsDays = 0;

    // Status
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

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

    public enum CustomerType {
        RETAIL,
        BUSINESS,
        CONTRACTOR
    }

    /**
     * Get display name for the customer
     */
    public String getDisplayName() {
        if (customerType == CustomerType.BUSINESS) {
            return companyName != null ? companyName : code;
        } else {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            } else if (lastName != null) {
                return lastName;
            } else {
                return code;
            }
        }
    }

    /**
     * Get search text for autocomplete (includes code, name, email, phone)
     */
    public String getSearchText() {
        StringBuilder sb = new StringBuilder();
        sb.append(code).append(" ");
        if (companyName != null) sb.append(companyName).append(" ");
        if (firstName != null) sb.append(firstName).append(" ");
        if (lastName != null) sb.append(lastName).append(" ");
        if (email != null) sb.append(email).append(" ");
        if (phone != null) sb.append(phone).append(" ");
        if (vatCode != null) sb.append(vatCode).append(" ");
        return sb.toString().toLowerCase();
    }
}
