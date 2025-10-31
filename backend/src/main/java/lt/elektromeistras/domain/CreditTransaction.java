package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Credit transaction for tracking customer credit purchases and returns
 * Used as replacement for manual notebook entries
 */
@Entity
@Table(name = "credit_transactions", indexes = {
        @Index(name = "idx_credit_customer_id", columnList = "customer_id"),
        @Index(name = "idx_credit_type", columnList = "transaction_type"),
        @Index(name = "idx_credit_status", columnList = "status"),
        @Index(name = "idx_credit_created_at", columnList = "created_at"),
        @Index(name = "idx_credit_customer_created", columnList = "customer_id,created_at"),
        @Index(name = "idx_credit_customer_status", columnList = "customer_id,status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CreditTransactionLine> lines = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    // Who performed the transaction (employee or customer self-service)
    @Column(name = "performed_by", nullable = false, length = 200)
    private String performedBy;

    @Column(name = "performed_by_user_id")
    private UUID performedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "performed_by_role", nullable = false, length = 20)
    private PerformedByRole performedByRole;

    // Signature/confirmation
    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureData;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "confirmed_by", length = 200)
    private String confirmedBy;

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
        if (transactionNumber == null) {
            generateTransactionNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private void generateTransactionNumber() {
        String prefix = transactionType == TransactionType.PICKUP ? "P" : "R";
        this.transactionNumber = prefix + System.currentTimeMillis();
    }

    public void addLine(CreditTransactionLine line) {
        lines.add(line);
        line.setTransaction(this);
        recalculateTotal();
    }

    public void removeLine(CreditTransactionLine line) {
        lines.remove(line);
        line.setTransaction(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = lines.stream()
                .map(line -> line.getLineTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalItems = lines.stream()
                .map(CreditTransactionLine::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }

    public void confirm(String confirmedBy) {
        this.status = TransactionStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
        this.confirmedBy = confirmedBy;
    }

    public enum TransactionType {
        PICKUP,  // Paėmimas prekių į skolą
        RETURN   // Grąžinimas prekių
    }

    public enum TransactionStatus {
        PENDING,     // Laukiama patvirtinimo
        CONFIRMED,   // Patvirtinta
        INVOICED,    // Į sąskaitą faktūrą įtraukta
        CANCELLED    // Atšaukta
    }

    public enum PerformedByRole {
        CUSTOMER,        // Klientas (savitarna)
        EMPLOYEE,        // Darbuotojas
        ADMINISTRATOR    // Administratorius
    }
}
