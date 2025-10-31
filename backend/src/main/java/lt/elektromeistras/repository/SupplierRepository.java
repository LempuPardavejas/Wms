package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Supplier entity
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    /**
     * Find supplier by code
     */
    Optional<Supplier> findByCode(String code);

    /**
     * Check if supplier exists by code
     */
    boolean existsByCode(String code);
}
