package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_lines", indexes = {
    @Index(name = "idx_orderline_order", columnList = "order_id"),
    @Index(name = "idx_orderline_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "is_cable")
    private Boolean isCable = false;

    @Column(name = "roll_id", length = 50)
    private String rollId;

    @Column(name = "cut_length", precision = 10, scale = 2)
    private BigDecimal cutLength;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.valueOf(21);

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    public void calculateLineTotal() {
        BigDecimal discountAmount = unitPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal priceAfterDiscount = unitPrice.subtract(discountAmount);
        this.lineTotal = priceAfterDiscount.multiply(quantity);
    }
}
