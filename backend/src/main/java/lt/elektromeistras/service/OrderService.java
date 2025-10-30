package lt.elektromeistras.service;

import lt.elektromeistras.domain.Customer;
import lt.elektromeistras.domain.Order;
import lt.elektromeistras.domain.OrderLine;
import lt.elektromeistras.domain.Product;
import lt.elektromeistras.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;

    /**
     * Get order by ID with all lines loaded
     */
    public Order getById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    /**
     * Get order by order number
     */
    public Order getByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
    }

    /**
     * Get orders by customer - CRITICAL for order selection window
     */
    public Page<Order> getOrdersByCustomer(UUID customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    /**
     * Get COMPLETED orders by customer - for returns selection
     * Returns list for quick access without pagination
     */
    public List<Order> getCompletedOrdersByCustomer(UUID customerId) {
        return orderRepository.findCompletedOrdersByCustomerId(customerId);
    }

    /**
     * Get all orders with pagination
     */
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * Search orders
     */
    public Page<Order> searchOrders(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.searchOrders(query.trim(), pageable);
    }

    /**
     * Get orders by status
     */
    public Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    /**
     * Create new order with lines
     */
    @Transactional
    public Order createOrder(Order order, List<OrderLine> orderLines) {
        log.info("Creating new order for customer: {}", order.getCustomer().getId());

        // Load customer
        Customer customer = customerService.getById(order.getCustomer().getId());
        order.setCustomer(customer);

        // Generate order number if not provided
        if (order.getOrderNumber() == null || order.getOrderNumber().trim().isEmpty()) {
            order.setOrderNumber(generateOrderNumber());
        }

        // Validate unique order number
        if (orderRepository.findByOrderNumber(order.getOrderNumber()).isPresent()) {
            throw new RuntimeException("Order with number already exists: " + order.getOrderNumber());
        }

        // Add order lines
        for (OrderLine line : orderLines) {
            // Load product
            Product product = productService.getById(line.getProduct().getId());
            line.initializeFromProduct(product);

            // Add to order
            order.addOrderLine(line);
        }

        // Calculate totals
        order.calculateTotals();

        // Save order (cascades to lines)
        return orderRepository.save(order);
    }

    /**
     * Create simple order with product codes and quantities
     * This is the FAST entry method for the new window
     */
    @Transactional
    public Order createQuickOrder(UUID customerId, List<QuickOrderLine> quickLines) {
        log.info("Creating quick order for customer: {}", customerId);

        Customer customer = customerService.getById(customerId);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(Order.OrderStatus.DRAFT);

        // Process each quick line
        for (QuickOrderLine quickLine : quickLines) {
            Product product = productService.getByCode(quickLine.getProductCode());

            OrderLine line = new OrderLine();
            line.initializeFromProduct(product);
            line.setQuantity(quickLine.getQuantity());

            // Calculate amounts
            line.calculateAmounts();

            order.addOrderLine(line);
        }

        // Calculate order totals
        order.calculateTotals();

        return orderRepository.save(order);
    }

    /**
     * Update order
     */
    @Transactional
    public Order updateOrder(UUID id, Order orderDetails) {
        log.info("Updating order with id: {}", id);

        Order order = getById(id);

        // Update fields
        order.setStatus(orderDetails.getStatus());
        order.setRequiredDate(orderDetails.getRequiredDate());
        order.setDeliveryDate(orderDetails.getDeliveryDate());
        order.setPaymentMethod(orderDetails.getPaymentMethod());
        order.setPaymentStatus(orderDetails.getPaymentStatus());
        order.setPaidAmount(orderDetails.getPaidAmount());
        order.setDeliveryAddress(orderDetails.getDeliveryAddress());
        order.setDeliveryCity(orderDetails.getDeliveryCity());
        order.setDeliveryPostalCode(orderDetails.getDeliveryPostalCode());
        order.setDeliveryNotes(orderDetails.getDeliveryNotes());
        order.setNotes(orderDetails.getNotes());

        return orderRepository.save(order);
    }

    /**
     * Confirm order (transition from DRAFT to CONFIRMED)
     */
    @Transactional
    public Order confirmOrder(UUID id) {
        log.info("Confirming order with id: {}", id);

        Order order = getById(id);

        if (order.getStatus() != Order.OrderStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT orders can be confirmed");
        }

        if (order.getOrderLines().isEmpty()) {
            throw new RuntimeException("Cannot confirm order with no line items");
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    /**
     * Cancel order
     */
    @Transactional
    public Order cancelOrder(UUID id, String reason) {
        log.info("Cancelling order with id: {}", id);

        Order order = getById(id);

        if (order.getStatus() == Order.OrderStatus.COMPLETED ||
            order.getStatus() == Order.OrderStatus.SHIPPED) {
            throw new RuntimeException("Cannot cancel completed or shipped orders");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") +
                       "Cancelled: " + reason);

        return orderRepository.save(order);
    }

    /**
     * Generate order number: ORD-YYYYMMDD-0001
     */
    private String generateOrderNumber() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Count orders created today
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        long todayCount = orderRepository.countOrdersSince(startOfDay);

        int sequenceNumber = (int) (todayCount + 1);

        return String.format("ORD-%s-%04d", datePart, sequenceNumber);
    }

    /**
     * Helper class for quick order entry
     */
    public static class QuickOrderLine {
        private String productCode;
        private BigDecimal quantity;

        public QuickOrderLine() {
        }

        public QuickOrderLine(String productCode, BigDecimal quantity) {
            this.productCode = productCode;
            this.quantity = quantity;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }
    }
}
