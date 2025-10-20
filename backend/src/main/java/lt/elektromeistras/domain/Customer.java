package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_code", columnList = "code"),
    @Index(name = "idx_customer_vat", columnList = "vat_code"),
    @Index(name = "idx_customer_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    private CustomerType customerType;

    @Column(name = "company_name", length = 300)
    private String companyName;

    @Column(name = "vat_code", length = 50)
    private String vatCode;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 200)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country = "Lithuania";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_group_id")
    private PriceGroup priceGroup;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "current_balance", precision = 12, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum CustomerType {
        RETAIL,
        BUSINESS,
        CONTRACTOR
    }
}
