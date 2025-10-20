package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByCode(String code);
    List<Category> findByIsActiveTrue();
    List<Category> findByParentIsNull();
}
