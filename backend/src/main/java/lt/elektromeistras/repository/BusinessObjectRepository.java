package lt.elektromeistras.repository;

import lt.elektromeistras.domain.BusinessObject;
import lt.elektromeistras.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessObjectRepository extends JpaRepository<BusinessObject, UUID> {

    Optional<BusinessObject> findByCode(String code);

    List<BusinessObject> findByIsActiveTrue();

    List<BusinessObject> findByDepartment(Department department);

    List<BusinessObject> findByObjectType(BusinessObject.ObjectType objectType);

    boolean existsByCode(String code);
}
