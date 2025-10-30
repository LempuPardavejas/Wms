package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Customer;
import lt.elektromeistras.domain.Order;
import lt.elektromeistras.domain.Return;
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
public interface ReturnRepository extends JpaRepository<Return, UUID> {
    Optional<Return> findByReturnNumber(String returnNumber);

    List<Return> findByOrder(Order order);

    List<Return> findByCustomer(Customer customer);
    Page<Return> findByCustomer(Customer customer, Pageable pageable);

    @Query("SELECT r FROM Return r WHERE r.status = :status")
    Page<Return> findByStatus(@Param("status") Return.ReturnStatus status, Pageable pageable);

    @Query("SELECT r FROM Return r WHERE r.refundStatus = :refundStatus")
    Page<Return> findByRefundStatus(@Param("refundStatus") Return.RefundStatus refundStatus, Pageable pageable);

    @Query("SELECT r FROM Return r WHERE r.returnDate BETWEEN :startDate AND :endDate")
    List<Return> findByReturnDateBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Return r WHERE r.returnDate >= :startDate")
    Long countReturnsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(r) FROM Return r WHERE r.order.id = :orderId")
    Long countReturnsByOrderId(@Param("orderId") UUID orderId);
}
