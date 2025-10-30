package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_lines", indexes = {
    @Index(name = "idx_return_line_return_id", columnList = "return_id"),
    @Index(name = "idx_return_line_product_id", columnList = "product_id"),
    @Index(name = "idx_return_line_condition", columnList = "condition"),
    @Index(name = "idx_return_line_restocked", columnList = "restocked")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private Return returnEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_line_id", nullable = false)
    private OrderLine orderLine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "return_reason_id", nullable = false)
    private ReturnReason returnReason;

    @Column(name = "quantity_ordered", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantityOrdered;

    @Column(name = "quantity_returned", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantityReturned;

    @Column(name = "quantity_accepted", precision = 19, scale = 3)
    private BigDecimal quantityAccepted = BigDecimal.ZERO;

    @Column(name = "quantity_rejected", precision = 19, scale = 3)
    private BigDecimal quantityRejected = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCondition condition = ProductCondition.UNKNOWN;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "refund_amount", precision = 19, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "restock_eligible", nullable = false)
    private Boolean restockEligible = false;

    @Column(name = "restocked", nullable = false)
    private Boolean restocked = false;

    @Column(name = "restocked_date")
    private LocalDateTime restockedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_location_id")
    private WarehouseLocation warehouseLocation;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "inspection_notes", columnDefinition = "TEXT")
    private String inspectionNotes;

    public void calculateLineTotal() {
        BigDecimal discountAmount = unitPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal priceAfterDiscount = unitPrice.subtract(discountAmount);
        this.lineTotal = priceAfterDiscount.multiply(quantityReturned);
    }

    public void calculateRefundAmount() {
        BigDecimal acceptedAmount = unitPrice.multiply(quantityAccepted);
        BigDecimal discountAmount = acceptedAmount.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal priceAfterDiscount = acceptedAmount.subtract(discountAmount);
        BigDecimal taxAmount = priceAfterDiscount.multiply(taxRate).divide(BigDecimal.valueOf(100));
        this.refundAmount = priceAfterDiscount.add(taxAmount);
    }

    public enum ProductCondition {
        UNKNOWN,      // Nežinoma (dar negauta)
        PERFECT,      // Tobula būklė
        GOOD,         // Gera būklė (galima parduoti kaip naudotą)
        DAMAGED,      // Pažeista (negali būti parduota)
        DEFECTIVE,    // Defektas (reikia grąžinti gamintojui)
        MISSING_PARTS // Trūksta dalių
    }
}
