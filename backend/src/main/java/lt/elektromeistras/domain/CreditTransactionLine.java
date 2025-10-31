package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Individual line item in a credit transaction
 */
@Entity
@Table(name = "credit_transaction_lines", indexes = {
        @Index(name = "idx_credit_line_transaction", columnList = "transaction_id"),
        @Index(name = "idx_credit_line_product", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private CreditTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        calculateLineTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateLineTotal();
    }

    private void calculateLineTotal() {
        this.lineTotal = unitPrice.multiply(quantity);
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateLineTotal();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }
}
