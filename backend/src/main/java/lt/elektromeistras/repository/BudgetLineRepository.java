package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Budget;
import lt.elektromeistras.domain.BudgetLine;
import lt.elektromeistras.domain.Department;
import lt.elektromeistras.domain.GLAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetLineRepository extends JpaRepository<BudgetLine, UUID> {

    List<BudgetLine> findByBudget(Budget budget);

    List<BudgetLine> findByGlAccount(GLAccount glAccount);

    List<BudgetLine> findByDepartment(Department department);

    List<BudgetLine> findByBudgetAndGlAccount(Budget budget, GLAccount glAccount);
}
