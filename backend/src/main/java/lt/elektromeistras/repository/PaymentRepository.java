package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByMatchedFalse();
    
    @Query("SELECT p FROM Payment p WHERE p.customer.id = :customerId ORDER BY p.paymentDate DESC")
    List<Payment> findByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT p FROM Payment p WHERE p.invoice.id = :invoiceId")
    List<Payment> findByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
