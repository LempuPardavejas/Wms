package lt.elektromeistras.controller;

import lombok.RequiredArgsConstructor;
import lt.elektromeistras.domain.Order;
import lt.elektromeistras.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<Page<Order>> getOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order created = orderService.createOrder(order);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<Order> confirmOrder(@PathVariable UUID id) {
        Order confirmed = orderService.confirmOrder(id);
        return ResponseEntity.ok(confirmed);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<Void> completeOrder(@PathVariable UUID id) {
        orderService.completeOrder(id);
        return ResponseEntity.ok().build();
    }
}
