package lt.elektromeistras.controller;

import lt.elektromeistras.domain.Order;
import lt.elektromeistras.dto.request.QuickOrderRequest;
import lt.elektromeistras.dto.response.OrderSummaryResponse;
import lt.elektromeistras.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Order> getById(@PathVariable UUID id) {
        Order order = orderService.getById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Get order by order number
     * GET /api/orders/number/ORD-20251028-0001
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAnyAuthority('SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Order> getByOrderNumber(@PathVariable String orderNumber) {
        Order order = orderService.getByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * Get orders by customer - CRITICAL for order selection
     * GET /api/orders/customer/{customerId}
     * Returns paginated list of orders
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Order>> getOrdersByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {
        Page<Order> orders = orderService.getOrdersByCustomer(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get COMPLETED orders by customer - for returns selection
     * GET /api/orders/customer/{customerId}/completed
     * Returns list without pagination for quick access
     */
    @GetMapping("/customer/{customerId}/completed")
    @PreAuthorize("hasAnyAuthority('SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<OrderSummaryResponse>> getCompletedOrdersByCustomer(
            @PathVariable UUID customerId) {
        List<Order> orders = orderService.getCompletedOrdersByCustomer(customerId);

        List<OrderSummaryResponse> response = orders.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders with pagination
     * GET /api/orders?page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Order>> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Search orders
     * GET /api/orders/search?q=ORD-2025
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Order>> searchOrders(
            @RequestParam String q,
            Pageable pageable) {
        Page<Order> orders = orderService.searchOrders(q, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by status
     * GET /api/orders/status/DRAFT
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Order>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status,
            Pageable pageable) {
        Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Create quick order - OPTIMIZED for fast entry
     * POST /api/orders/quick
     * Body: {
     *   "customerId": "uuid",
     *   "lines": [
     *     {"productCode": "0010006", "quantity": 100},
     *     {"productCode": "0020001", "quantity": 10}
     *   ]
     * }
     */
    @PostMapping("/quick")
    @PreAuthorize("hasAnyAuthority('SALES_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<Order> createQuickOrder(@RequestBody QuickOrderRequest request) {
        log.info("Creating quick order for customer: {}", request.getCustomerId());

        List<OrderService.QuickOrderLine> quickLines = request.getLines().stream()
                .map(line -> new OrderService.QuickOrderLine(line.getProductCode(), line.getQuantity()))
                .collect(Collectors.toList());

        Order order = orderService.createQuickOrder(request.getCustomerId(), quickLines);

        // Set notes if provided
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            order.setNotes(request.getNotes());
        }

        // Set project if provided
        if (request.getProjectId() != null) {
            order.setProjectId(request.getProjectId());
        }

        return ResponseEntity.ok(order);
    }

    /**
     * Update order
     * PUT /api/orders/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SALES_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Order> updateOrder(
            @PathVariable UUID id,
            @RequestBody Order order) {
        Order updated = orderService.updateOrder(id, order);
        return ResponseEntity.ok(updated);
    }

    /**
     * Confirm order
     * POST /api/orders/{id}/confirm
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('SALES_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Order> confirmOrder(@PathVariable UUID id) {
        Order confirmed = orderService.confirmOrder(id);
        return ResponseEntity.ok(confirmed);
    }

    /**
     * Cancel order
     * POST /api/orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('SALES_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable UUID id,
            @RequestBody(required = false) String reason) {
        Order cancelled = orderService.cancelOrder(id, reason != null ? reason : "No reason provided");
        return ResponseEntity.ok(cancelled);
    }

    /**
     * Map Order to lightweight OrderSummaryResponse
     */
    private OrderSummaryResponse mapToSummaryResponse(Order o) {
        return new OrderSummaryResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getOrderDate(),
                o.getStatus().name(),
                o.getPaymentStatus().name(),
                o.getTotalAmount(),
                o.getPaidAmount(),
                o.getOrderLines().size()
        );
    }
}
