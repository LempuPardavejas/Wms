package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Find customer by code - FAST lookup with index
     */
    Optional<Customer> findByCode(String code);

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customer by VAT code
     */
    Optional<Customer> findByVatCode(String vatCode);

    /**
     * Find all active customers
     */
    Page<Customer> findByIsActiveTrue(Pageable pageable);

    /**
     * Find customers by type
     */
    Page<Customer> findByCustomerTypeAndIsActiveTrue(Customer.CustomerType customerType, Pageable pageable);

    /**
     * FAST customer search - CRITICAL for autocomplete
     * Searches in code, company name, first/last name, email, phone, VAT code
     * Uses indexed columns for performance
     * Prioritizes exact code matches, then company name matches
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.isActive = true AND (" +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.mobile) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.vatCode) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ") ORDER BY " +
            "CASE WHEN LOWER(c.code) = LOWER(:query) THEN 1 " +
            "WHEN LOWER(c.code) LIKE LOWER(CONCAT(:query, '%')) THEN 2 " +
            "WHEN LOWER(c.companyName) LIKE LOWER(CONCAT(:query, '%')) THEN 3 " +
            "WHEN LOWER(c.lastName) LIKE LOWER(CONCAT(:query, '%')) THEN 4 " +
            "ELSE 5 END, c.companyName, c.lastName")
    List<Customer> searchCustomers(@Param("query") String query);

    /**
     * FAST customer search with pagination
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.isActive = true AND (" +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.mobile) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.vatCode) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    Page<Customer> searchCustomers(@Param("query") String query, Pageable pageable);

    /**
     * Search customers by code prefix - optimized for dropdown
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.isActive = true AND " +
            "LOWER(c.code) LIKE LOWER(CONCAT(:codePrefix, '%')) " +
            "ORDER BY c.code")
    List<Customer> findByCodeStartingWith(@Param("codePrefix") String codePrefix);

    /**
     * Get customers with overdue balance (current balance > credit limit)
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.isActive = true AND " +
            "c.currentBalance > c.creditLimit")
    List<Customer> findCustomersOverCreditLimit();
}
