package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "order_number"),
        @Index(name = "idx_order_customer", columnList = "customer_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_date", columnList = "order_date"),
        @Index(name = "idx_order_customer_status", columnList = "customer_id,status"),
        @Index(name = "idx_order_customer_date", columnList = "customer_id,order_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "project_id")
    private UUID projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    // Financial
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Payment
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    // Delivery
    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "delivery_city", length = 100)
    private String deliveryCity;

    @Column(name = "delivery_postal_code", length = 20)
    private String deliveryPostalCode;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    // Users
    @Column(name = "sales_person_id")
    private UUID salesPersonId;

    @Column(name = "created_by_id")
    private UUID createdById;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Order lines - eager loading for convenience
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("lineNumber ASC")
    private List<OrderLine> orderLines = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (orderDate == null) {
            orderDate = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum OrderStatus {
        DRAFT,
        CONFIRMED,
        PROCESSING,
        PICKING,
        PACKED,
        SHIPPED,
        COMPLETED,
        CANCELLED
    }

    public enum PaymentStatus {
        UNPAID,
        PARTIAL,
        PAID,
        OVERPAID,
        REFUNDED
    }

    /**
     * Calculate order totals from line items
     */
    public void calculateTotals() {
        subtotal = BigDecimal.ZERO;
        taxAmount = BigDecimal.ZERO;
        totalAmount = BigDecimal.ZERO;

        for (OrderLine line : orderLines) {
            BigDecimal lineSubtotal = line.getUnitPrice()
                    .multiply(line.getQuantity())
                    .subtract(line.getDiscountAmount());
            subtotal = subtotal.add(lineSubtotal);
            taxAmount = taxAmount.add(line.getTaxAmount());
        }

        totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
    }

    /**
     * Add order line
     */
    public void addOrderLine(OrderLine orderLine) {
        orderLines.add(orderLine);
        orderLine.setOrder(this);
        orderLine.setLineNumber(orderLines.size());
    }

    /**
     * Remove order line
     */
    public void removeOrderLine(OrderLine orderLine) {
        orderLines.remove(orderLine);
        orderLine.setOrder(null);
        // Renumber lines
        for (int i = 0; i < orderLines.size(); i++) {
            orderLines.get(i).setLineNumber(i + 1);
        }
    }

    /**
     * Get order lines - alias for getOrderLines() for backward compatibility
     */
    public List<OrderLine> getLines() {
        return orderLines;
    }
}
