package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission entity operations
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Find permission by code
     */
    Optional<Permission> findByCode(String code);

    /**
     * Find permissions by category
     */
    List<Permission> findByCategory(String category);

    /**
     * Check if permission code exists
     */
    boolean existsByCode(String code);
}
