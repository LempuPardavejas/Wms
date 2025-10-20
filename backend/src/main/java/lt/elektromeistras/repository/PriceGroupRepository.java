package lt.elektromeistras.repository;

import lt.elektromeistras.domain.PriceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceGroupRepository extends JpaRepository<PriceGroup, UUID> {
    Optional<PriceGroup> findByCode(String code);
    List<PriceGroup> findByIsActiveTrue();
}
