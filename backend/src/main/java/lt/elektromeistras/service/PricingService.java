package lt.elektromeistras.service;

import lombok.RequiredArgsConstructor;
import lt.elektromeistras.domain.Customer;
import lt.elektromeistras.domain.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing Engine Service
 * Implements the pricing logic as specified in the business requirements
 */
@Service
@RequiredArgsConstructor
public class PricingService {

    public BigDecimal calculatePrice(Product product, Customer customer, BigDecimal quantity) {
        // 1. Get base price
        BigDecimal basePrice = product.getBasePrice();
        if (basePrice == null) {
            throw new IllegalStateException("Product " + product.getSku() + " has no base price");
        }

        // 2. Apply price group discount
        if (customer != null && customer.getPriceGroup() != null) {
            basePrice = applyPriceGroupDiscount(basePrice, customer);
        }

        // 3. Check quantity discounts (future implementation)
        basePrice = applyQuantityDiscount(basePrice, product, quantity);

        // 4. Check active promotions (future implementation)
        basePrice = applyPromotions(basePrice, product, customer);

        return basePrice.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyPriceGroupDiscount(BigDecimal basePrice, Customer customer) {
        if (customer.getPriceGroup() == null || customer.getPriceGroup().getDiscountPercentage() == null) {
            return basePrice;
        }

        BigDecimal discountPercentage = customer.getPriceGroup().getDiscountPercentage();
        BigDecimal discountAmount = basePrice.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return basePrice.subtract(discountAmount);
    }

    private BigDecimal applyQuantityDiscount(BigDecimal basePrice, Product product, BigDecimal quantity) {
        // TODO: Implement quantity discount logic
        // This would check product-specific quantity break rules
        return basePrice;
    }

    private BigDecimal applyPromotions(BigDecimal basePrice, Product product, Customer customer) {
        // TODO: Implement promotional pricing logic
        // This would check for active promotions applicable to product/customer
        return basePrice;
    }

    public BigDecimal calculateTax(BigDecimal amount, BigDecimal taxRate) {
        return amount.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateLineTotal(BigDecimal unitPrice, BigDecimal quantity, 
                                         BigDecimal discountPercentage) {
        BigDecimal discountAmount = unitPrice.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal priceAfterDiscount = unitPrice.subtract(discountAmount);
        return priceAfterDiscount.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }
}
