package lt.elektromeistras.repository;

import lt.elektromeistras.domain.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetPeriodRepository extends JpaRepository<BudgetPeriod, UUID> {

    Optional<BudgetPeriod> findByCode(String code);

    List<BudgetPeriod> findByFiscalYear(Integer fiscalYear);

    List<BudgetPeriod> findByPeriodType(BudgetPeriod.PeriodType periodType);

    List<BudgetPeriod> findByStatus(BudgetPeriod.PeriodStatus status);

    List<BudgetPeriod> findByIsActiveTrue();

    @Query("SELECT bp FROM BudgetPeriod bp WHERE :date BETWEEN bp.startDate AND bp.endDate")
    List<BudgetPeriod> findByDate(@Param("date") LocalDate date);

    boolean existsByCode(String code);
}
