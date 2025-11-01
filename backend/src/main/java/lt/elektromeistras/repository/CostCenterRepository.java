package lt.elektromeistras.repository;

import lt.elektromeistras.domain.CostCenter;
import lt.elektromeistras.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, UUID> {

    Optional<CostCenter> findByCode(String code);

    List<CostCenter> findByIsActiveTrue();

    List<CostCenter> findByDepartment(Department department);

    List<CostCenter> findByCenterType(CostCenter.CenterType centerType);

    boolean existsByCode(String code);
}
