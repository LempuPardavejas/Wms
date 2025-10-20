package lt.elektromeistras.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.elektromeistras.domain.*;
import lt.elektromeistras.exception.InsufficientStockException;
import lt.elektromeistras.repository.ProductStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Stock Management Service
 * Handles inventory operations including stock reservation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final ProductStockRepository stockRepository;

    @Transactional
    public void reserveStock(Order order) {
        log.info("Reserving stock for order: {}", order.getOrderNumber());

        for (OrderLine line : order.getLines()) {
            ProductStock stock = stockRepository.findByProductAndWarehouse(
                    line.getProduct(), 
                    order.getWarehouse()
            ).orElseThrow(() -> new InsufficientStockException(
                    "No stock found for product: " + line.getProduct().getSku()
            ));

            BigDecimal availableQuantity = stock.getAvailableQuantity();
            if (availableQuantity.compareTo(line.getQuantity()) < 0) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product %s. Available: %s, Required: %s",
                                line.getProduct().getSku(),
                                availableQuantity,
                                line.getQuantity())
                );
            }

            stock.setReservedQuantity(
                    stock.getReservedQuantity().add(line.getQuantity())
            );
            stockRepository.save(stock);

            log.debug("Reserved {} units of {} for order {}", 
                    line.getQuantity(), line.getProduct().getSku(), order.getOrderNumber());
        }
    }

    @Transactional
    public void releaseStock(Order order) {
        log.info("Releasing stock for order: {}", order.getOrderNumber());

        for (OrderLine line : order.getLines()) {
            ProductStock stock = stockRepository.findByProductAndWarehouse(
                    line.getProduct(),
                    order.getWarehouse()
            ).orElse(null);

            if (stock != null) {
                stock.setReservedQuantity(
                        stock.getReservedQuantity().subtract(line.getQuantity())
                );
                stockRepository.save(stock);

                log.debug("Released {} units of {} from order {}",
                        line.getQuantity(), line.getProduct().getSku(), order.getOrderNumber());
            }
        }
    }

    @Transactional
    public void commitStock(Order order) {
        log.info("Committing stock for order: {}", order.getOrderNumber());

        for (OrderLine line : order.getLines()) {
            ProductStock stock = stockRepository.findByProductAndWarehouse(
                    line.getProduct(),
                    order.getWarehouse()
            ).orElseThrow(() -> new InsufficientStockException(
                    "No stock found for product: " + line.getProduct().getSku()
            ));

            // Reduce both quantity and reserved quantity
            stock.setQuantity(stock.getQuantity().subtract(line.getQuantity()));
            stock.setReservedQuantity(stock.getReservedQuantity().subtract(line.getQuantity()));
            stockRepository.save(stock);

            // Handle cable roll updates if applicable
            if (Boolean.TRUE.equals(line.getIsCable()) && line.getCutLength() != null) {
                updateCableRoll(stock, line.getCutLength());
            }

            log.debug("Committed {} units of {} for order {}",
                    line.getQuantity(), line.getProduct().getSku(), order.getOrderNumber());
        }
    }

    private void updateCableRoll(ProductStock stock, BigDecimal cutLength) {
        if (stock.getRollCurrentLength() != null) {
            BigDecimal newLength = stock.getRollCurrentLength().subtract(cutLength);
            stock.setRollCurrentLength(newLength);
            log.debug("Updated cable roll {} from {} to {}",
                    stock.getRollId(), stock.getRollCurrentLength(), newLength);
        }
    }

    public BigDecimal getAvailableStock(UUID productId, UUID warehouseId) {
        return stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .map(ProductStock::getAvailableQuantity)
                .orElse(BigDecimal.ZERO);
    }

    public List<ProductStock> getLowStockItems() {
        return stockRepository.findLowStockItems();
    }

    @Transactional
    public void adjustStock(UUID productId, UUID warehouseId, BigDecimal quantity, String reason) {
        ProductStock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        stock.setQuantity(stock.getQuantity().add(quantity));
        stockRepository.save(stock);

        log.info("Stock adjusted for product {} by {}. Reason: {}", productId, quantity, reason);
    }
}
