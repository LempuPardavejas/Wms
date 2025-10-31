package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Role entity operations
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find role by code
     */
    Optional<Role> findByCode(String code);

    /**
     * Find all active roles
     */
    List<Role> findByIsActiveTrue();

    /**
     * Check if role code exists
     */
    boolean existsByCode(String code);
}
