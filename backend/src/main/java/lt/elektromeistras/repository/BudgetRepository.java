package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Budget;
import lt.elektromeistras.domain.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByCode(String code);

    List<Budget> findByBudgetPeriod(BudgetPeriod budgetPeriod);

    List<Budget> findByBudgetType(Budget.BudgetType budgetType);

    List<Budget> findByStatus(Budget.BudgetStatus status);

    List<Budget> findByBudgetPeriodAndStatus(BudgetPeriod budgetPeriod, Budget.BudgetStatus status);

    boolean existsByCode(String code);
}
