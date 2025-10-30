package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find order by order number - FAST lookup with index
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by customer - CRITICAL for order selection in client window
     * Uses composite index on customer_id + status for performance
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.orderDate DESC")
    Page<Order> findByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    /**
     * Find orders by customer and status
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.status = :status ORDER BY o.orderDate DESC")
    Page<Order> findByCustomerIdAndStatus(@Param("customerId") UUID customerId,
                                          @Param("status") Order.OrderStatus status,
                                          Pageable pageable);

    /**
     * Find COMPLETED orders by customer - for returns selection
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.status = 'COMPLETED' ORDER BY o.orderDate DESC")
    List<Order> findCompletedOrdersByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Find orders by status
     */
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    /**
     * Find orders by payment status
     */
    Page<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Find orders by project
     */
    @Query("SELECT o FROM Order o WHERE o.projectId = :projectId ORDER BY o.orderDate DESC")
    Page<Order> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Search orders by number or customer name
     */
    @Query("SELECT o FROM Order o WHERE " +
            "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(o.customer.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(o.customer.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Order> searchOrders(@Param("query") String query, Pageable pageable);

    /**
     * Find orders within date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    Page<Order> findOrdersByDateRange(@Param("startDate") Instant startDate,
                                      @Param("endDate") Instant endDate,
                                      Pageable pageable);

    /**
     * Count orders created today for order number generation
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startOfDay")
    long countOrdersSince(@Param("startOfDay") Instant startOfDay);

    /**
     * Get unpaid orders by customer
     */
    @Query("SELECT o FROM Order o WHERE " +
            "o.customer.id = :customerId AND " +
            "o.paymentStatus IN ('UNPAID', 'PARTIAL') " +
            "ORDER BY o.orderDate DESC")
    List<Order> findUnpaidOrdersByCustomerId(@Param("customerId") UUID customerId);
}
