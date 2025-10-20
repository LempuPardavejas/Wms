package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Customer;
import lt.elektromeistras.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomer(Customer customer);
    Page<Order> findByCustomer(Customer customer, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    Page<Order> findByStatus(@Param("status") Order.OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startDate")
    Long countOrdersSince(@Param("startDate") LocalDateTime startDate);
}
