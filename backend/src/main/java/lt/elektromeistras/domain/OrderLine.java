package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_lines", indexes = {
        @Index(name = "idx_order_line_order", columnList = "order_id"),
        @Index(name = "idx_order_line_product", columnList = "product_id"),
        @Index(name = "idx_order_line_number", columnList = "order_id,line_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;

    // Quantity
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;

    // Cable specific
    @Column(name = "is_cable", nullable = false)
    private Boolean isCable = false;

    @Column(name = "roll_id")
    private UUID rollId;

    @Column(name = "cut_length", precision = 19, scale = 3)
    private BigDecimal cutLength;

    // Pricing
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    // Status tracking
    @Column(name = "quantity_picked", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantityPicked = BigDecimal.ZERO;

    @Column(name = "quantity_shipped", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantityShipped = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        calculateAmounts();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateAmounts();
    }

    /**
     * Calculate line amounts (discount, tax, total)
     */
    public void calculateAmounts() {
        // Calculate discount amount if percentage is set
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal lineSubtotal = unitPrice.multiply(quantity);
            discountAmount = lineSubtotal
                    .multiply(discountPercentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        // Calculate subtotal after discount
        BigDecimal subtotal = unitPrice
                .multiply(quantity)
                .subtract(discountAmount);

        // Calculate tax
        taxAmount = subtotal
                .multiply(taxRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Calculate line total
        lineTotal = subtotal.add(taxAmount);
    }

    /**
     * Calculate line total without tax
     */
    public BigDecimal getSubtotal() {
        return unitPrice
                .multiply(quantity)
                .subtract(discountAmount);
    }

    /**
     * Initialize from product
     */
    public void initializeFromProduct(Product product) {
        this.product = product;
        this.productCode = product.getCode();
        this.productName = product.getName();
        this.unitOfMeasure = product.getUnitOfMeasure();
        this.isCable = product.getIsCable();
        this.unitPrice = product.getBasePrice();
        this.taxRate = product.getTaxRate();
    }
}
