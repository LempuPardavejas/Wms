package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_customer", columnList = "customer_id"),
    @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
    @Index(name = "idx_payment_date", columnList = "payment_date"),
    @Index(name = "idx_payment_matched", columnList = "matched")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "bank_reference", length = 200)
    private String bankReference;

    @Column
    private Boolean matched = false;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
