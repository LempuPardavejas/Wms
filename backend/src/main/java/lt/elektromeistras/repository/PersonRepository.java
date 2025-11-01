package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Department;
import lt.elektromeistras.domain.Person;
import lt.elektromeistras.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    Optional<Person> findByCode(String code);

    Optional<Person> findByUser(User user);

    List<Person> findByIsActiveTrue();

    List<Person> findByDepartment(Department department);

    List<Person> findByPersonType(Person.PersonType personType);

    boolean existsByCode(String code);
}
