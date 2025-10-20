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
    Optional<Customer> findByCode(String code);
    Optional<Customer> findByVatCode(String vatCode);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByIsActiveTrue();
    Page<Customer> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND " +
           "(LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.vatCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchCustomers(@Param("search") String search, Pageable pageable);
}
