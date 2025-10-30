package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "returns", indexes = {
    @Index(name = "idx_return_number", columnList = "return_number"),
    @Index(name = "idx_return_order_id", columnList = "order_id"),
    @Index(name = "idx_return_customer_id", columnList = "customer_id"),
    @Index(name = "idx_return_status", columnList = "status"),
    @Index(name = "idx_return_date", columnList = "return_date"),
    @Index(name = "idx_refund_status", columnList = "refund_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return extends BaseEntity {

    @Column(name = "return_number", nullable = false, unique = true, length = 50)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReturnStatus status = ReturnStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", nullable = false, length = 50)
    private ReturnType returnType = ReturnType.FULL;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "inspected_date")
    private LocalDateTime inspectedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "refund_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "refund_method", length = 50)
    private String refundMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", length = 50)
    private RefundStatus refundStatus = RefundStatus.PENDING;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "refund_reference")
    private String refundReference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspected_by")
    private User inspectedBy;

    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnLine> lines = new ArrayList<>();

    public void addLine(ReturnLine line) {
        lines.add(line);
        line.setReturnEntity(this);
    }

    public void removeLine(ReturnLine line) {
        lines.remove(line);
        line.setReturnEntity(null);
    }

    public enum ReturnStatus {
        PENDING,      // Pateikta, laukia patvirtinimo
        APPROVED,     // Patvirtinta
        IN_TRANSIT,   // Siunčiama atgal
        RECEIVED,     // Gauta sandėlyje
        INSPECTED,    // Patikrinta
        COMPLETED,    // Užbaigta (pinigai grąžinti)
        REJECTED      // Atmesta
    }

    public enum ReturnType {
        FULL,         // Pilnas užsakymo grąžinimas
        PARTIAL       // Dalinis grąžinimas
    }

    public enum RefundStatus {
        PENDING,      // Laukiama
        PROCESSING,   // Vykdoma
        COMPLETED,    // Užbaigta
        FAILED,       // Nepavyko
        CANCELLED     // Atšaukta
    }
}
