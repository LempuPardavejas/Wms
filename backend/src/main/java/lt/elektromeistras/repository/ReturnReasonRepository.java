package lt.elektromeistras.repository;

import lt.elektromeistras.domain.ReturnReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReturnReasonRepository extends JpaRepository<ReturnReason, UUID> {
    Optional<ReturnReason> findByCode(String code);
    List<ReturnReason> findByActiveTrue();
}
