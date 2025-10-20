package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoice_number", columnList = "invoice_number"),
    @Index(name = "idx_invoice_customer", columnList = "customer_id"),
    @Index(name = "idx_invoice_date", columnList = "invoice_date"),
    @Index(name = "idx_invoice_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum InvoiceStatus {
        DRAFT,
        ISSUED,
        SENT,
        PARTIAL,
        PAID,
        OVERDUE,
        CANCELLED
    }

    public BigDecimal getBalanceDue() {
        return totalAmount.subtract(paidAmount);
    }
}
