package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Warehouse entity operations.
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    /**
     * Find warehouse by unique code
     * @param code Warehouse code
     * @return Optional containing warehouse if found
     */
    Optional<Warehouse> findByCode(String code);

    /**
     * Find all active warehouses
     * @return List of active warehouses
     */
    List<Warehouse> findByIsActiveTrue();

    /**
     * Check if warehouse exists by code
     * @param code Warehouse code
     * @return true if exists
     */
    boolean existsByCode(String code);
}
