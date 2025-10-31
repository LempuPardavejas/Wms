package lt.elektromeistras.repository;

import lt.elektromeistras.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ProductCategory entity
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    /**
     * Find category by code
     */
    Optional<ProductCategory> findByCode(String code);

    /**
     * Check if category exists by code
     */
    boolean existsByCode(String code);
}
