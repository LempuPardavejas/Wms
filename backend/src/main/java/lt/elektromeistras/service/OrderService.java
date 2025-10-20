package lt.elektromeistras.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.elektromeistras.domain.*;
import lt.elektromeistras.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Order Management Service
 * Handles order lifecycle and calculations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockService stockService;
    private final PricingService pricingService;

    @Transactional
    public Order createOrder(Order order) {
        // Generate order number if not set
        if (order.getOrderNumber() == null) {
            order.setOrderNumber(generateOrderNumber());
        }

        // Set order date if not set
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }

        // Calculate totals
        calculateOrderTotals(order);

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Created order: {}", savedOrder.getOrderNumber());

        return savedOrder;
    }

    @Transactional
    public Order confirmOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != Order.OrderStatus.DRAFT) {
            throw new IllegalStateException("Only draft orders can be confirmed");
        }

        // Reserve stock
        stockService.reserveStock(order);

        // Update status
        order.setStatus(Order.OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        log.info("Confirmed order: {}", order.getOrderNumber());
        return savedOrder;
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() == Order.OrderStatus.CONFIRMED || 
            order.getStatus() == Order.OrderStatus.PROCESSING) {
            // Release reserved stock
            stockService.releaseStock(order);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Cancelled order: {}", order.getOrderNumber());
    }

    @Transactional
    public void completeOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Commit stock (reduce actual inventory)
        stockService.commitStock(order);

        order.setStatus(Order.OrderStatus.COMPLETED);
        orderRepository.save(order);

        log.info("Completed order: {}", order.getOrderNumber());
    }

    public void calculateOrderTotals(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (OrderLine line : order.getLines()) {
            // Calculate line total if not set
            if (line.getLineTotal() == null) {
                line.calculateLineTotal();
            }

            subtotal = subtotal.add(line.getLineTotal());
            
            // Calculate tax for this line
            BigDecimal lineTax = pricingService.calculateTax(line.getLineTotal(), line.getTaxRate());
            taxAmount = taxAmount.add(lineTax);
        }

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(subtotal.add(taxAmount));
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.countOrdersSince(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
        );
        return String.format("ORD-%s-%04d", timestamp, count + 1);
    }

    public Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    public Page<Order> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
}
