package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Budget;
import lt.elektromeistras.domain.BudgetVariance;
import lt.elektromeistras.domain.Department;
import lt.elektromeistras.domain.GLAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetVarianceRepository extends JpaRepository<BudgetVariance, UUID> {

    List<BudgetVariance> findByBudget(Budget budget);

    List<BudgetVariance> findByGlAccount(GLAccount glAccount);

    List<BudgetVariance> findByDepartment(Department department);

    List<BudgetVariance> findByVarianceType(BudgetVariance.VarianceType varianceType);

    @Query("SELECT bv FROM BudgetVariance bv WHERE bv.varianceDate BETWEEN :startDate AND :endDate")
    List<BudgetVariance> findByVarianceDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT bv FROM BudgetVariance bv WHERE bv.budget = :budget AND bv.glAccount = :glAccount")
    List<BudgetVariance> findByBudgetAndGlAccount(
            @Param("budget") Budget budget,
            @Param("glAccount") GLAccount glAccount
    );
}
