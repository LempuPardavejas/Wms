package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('ISSUED', 'PARTIAL') AND i.dueDate < CURRENT_DATE")
    Page<Invoice> findOverdueInvoices(Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = 'ISSUED' OR i.status = 'PARTIAL'")
    List<Invoice> findUnpaidInvoices();
    
    @Query("SELECT i FROM Invoice i WHERE i.customer.id = :customerId AND i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByCustomerAndDateRange(@Param("customerId") UUID customerId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
