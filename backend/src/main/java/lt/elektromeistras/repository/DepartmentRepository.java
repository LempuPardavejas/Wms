package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByCode(String code);

    List<Department> findByIsActiveTrue();

    List<Department> findByParentDepartment(Department parentDepartment);

    List<Department> findByParentDepartmentIsNull();

    boolean existsByCode(String code);
}
