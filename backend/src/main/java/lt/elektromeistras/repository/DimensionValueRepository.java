package lt.elektromeistras.repository;

import lt.elektromeistras.domain.DimensionType;
import lt.elektromeistras.domain.DimensionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DimensionValueRepository extends JpaRepository<DimensionValue, UUID> {

    List<DimensionValue> findByDimensionType(DimensionType dimensionType);

    Optional<DimensionValue> findByDimensionTypeAndCode(DimensionType dimensionType, String code);

    List<DimensionValue> findByDimensionTypeAndIsActiveTrue(DimensionType dimensionType);

    List<DimensionValue> findByParentValue(DimensionValue parentValue);

    boolean existsByDimensionTypeAndCode(DimensionType dimensionType, String code);
}
