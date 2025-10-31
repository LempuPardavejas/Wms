package lt.elektromeistras.repository;

import lt.elektromeistras.domain.CreditTransaction;
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
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {

    /**
     * Find transaction by transaction number
     */
    Optional<CreditTransaction> findByTransactionNumber(String transactionNumber);

    /**
     * Find all transactions for a customer
     */
    Page<CreditTransaction> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Find all transactions for a customer by type
     */
    Page<CreditTransaction> findByCustomerIdAndTransactionType(
            UUID customerId,
            CreditTransaction.TransactionType transactionType,
            Pageable pageable
    );

    /**
     * Find all transactions for a customer by status
     */
    Page<CreditTransaction> findByCustomerIdAndStatus(
            UUID customerId,
            CreditTransaction.TransactionStatus status,
            Pageable pageable
    );

    /**
     * Find all transactions by type
     */
    Page<CreditTransaction> findByTransactionType(
            CreditTransaction.TransactionType transactionType,
            Pageable pageable
    );

    /**
     * Find all transactions by status
     */
    Page<CreditTransaction> findByStatus(
            CreditTransaction.TransactionStatus status,
            Pageable pageable
    );

    /**
     * Find transactions within date range
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
            "ct.createdAt >= :startDate AND ct.createdAt <= :endDate " +
            "ORDER BY ct.createdAt DESC")
    List<CreditTransaction> findByDateRange(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Find transactions for customer within date range - for monthly statements
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
            "ct.customer.id = :customerId AND " +
            "ct.createdAt >= :startDate AND ct.createdAt <= :endDate AND " +
            "ct.status IN ('CONFIRMED', 'INVOICED') " +
            "ORDER BY ct.createdAt ASC")
    List<CreditTransaction> findByCustomerAndDateRange(
            @Param("customerId") UUID customerId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Find all confirmed transactions for a customer (for monthly statements)
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
            "ct.customer.id = :customerId AND " +
            "ct.status IN ('CONFIRMED', 'INVOICED') " +
            "ORDER BY ct.createdAt DESC")
    List<CreditTransaction> findConfirmedByCustomer(@Param("customerId") UUID customerId);

    /**
     * Find pending transactions for a customer
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
            "ct.customer.id = :customerId AND " +
            "ct.status = 'PENDING' " +
            "ORDER BY ct.createdAt DESC")
    List<CreditTransaction> findPendingByCustomer(@Param("customerId") UUID customerId);

    /**
     * Find transactions by customer code - for quick lookup
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
            "LOWER(ct.customer.code) = LOWER(:customerCode) " +
            "ORDER BY ct.createdAt DESC")
    List<CreditTransaction> findByCustomerCode(@Param("customerCode") String customerCode);

    /**
     * Find recent transactions for a customer (last N)
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
            "ct.customer.id = :customerId " +
            "ORDER BY ct.createdAt DESC")
    List<CreditTransaction> findRecentByCustomer(
            @Param("customerId") UUID customerId,
            Pageable pageable
    );

    /**
     * Count pending transactions for customer
     */
    long countByCustomerIdAndStatus(UUID customerId, CreditTransaction.TransactionStatus status);

    /**
     * Search transactions across multiple fields
     */
    @Query("SELECT ct FROM CreditTransaction ct WHERE " +
            "LOWER(ct.transactionNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(ct.customer.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(ct.customer.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(ct.performedBy) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY ct.createdAt DESC")
    Page<CreditTransaction> searchTransactions(@Param("query") String query, Pageable pageable);
}
