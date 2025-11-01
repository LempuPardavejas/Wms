package lt.elektromeistras.repository;

import lt.elektromeistras.domain.DimensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DimensionTypeRepository extends JpaRepository<DimensionType, UUID> {

    Optional<DimensionType> findByCode(String code);

    List<DimensionType> findByIsActiveTrue();

    boolean existsByCode(String code);
}
