package lt.elektromeistras.service;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for calculating and managing budget variances
 * Compares budgeted amounts with actual GL postings
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BudgetVarianceService {

    private final BudgetVarianceRepository budgetVarianceRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GLAccountRepository glAccountRepository;

    /**
     * Calculate variances for a budget
     */
    @Transactional
    public List<BudgetVariance> calculateVariancesForBudget(UUID budgetId) {
        log.info("Calculating variances for budget: {}", budgetId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));

        if (budget.getStatus() != Budget.BudgetStatus.ACTIVE) {
            throw new RuntimeException("Can only calculate variances for ACTIVE budgets");
        }

        List<BudgetVariance> variances = new ArrayList<>();

        // Get budget period dates
        LocalDate startDate = budget.getBudgetPeriod().getStartDate();
        LocalDate endDate = budget.getBudgetPeriod().getEndDate();
        LocalDate varianceDate = LocalDate.now();

        // Group budget lines by GL account and dimensions
        List<BudgetLine> budgetLines = budgetLineRepository.findByBudget(budget);

        for (BudgetLine budgetLine : budgetLines) {
            // Calculate actual amount from journal entries
            BigDecimal actualAmount = calculateActualAmount(
                    budgetLine.getGlAccount(),
                    budgetLine.getDepartment(),
                    budgetLine.getCostCenter(),
                    startDate,
                    varianceDate
            );

            // Create variance record
            BudgetVariance variance = BudgetVariance.builder()
                    .budget(budget)
                    .budgetLine(budgetLine)
                    .glAccount(budgetLine.getGlAccount())
                    .varianceDate(varianceDate)
                    .budgetedAmount(budgetLine.getAmount())
                    .actualAmount(actualAmount)
                    .department(budgetLine.getDepartment())
                    .businessObject(budgetLine.getBusinessObject())
                    .costCenter(budgetLine.getCostCenter())
                    .series(budgetLine.getSeries())
                    .person(budgetLine.getPerson())
                    .build();

            variance.calculateVariance();

            variances.add(budgetVarianceRepository.save(variance));
        }

        log.info("Calculated {} variances for budget {}", variances.size(), budget.getCode());

        return variances;
    }

    /**
     * Calculate actual amount from journal entries for specific criteria
     */
    private BigDecimal calculateActualAmount(
            GLAccount glAccount,
            Department department,
            CostCenter costCenter,
            LocalDate startDate,
            LocalDate endDate) {

        // Get all posted journal entry lines for the GL account
        List<JournalEntryLine> lines = journalEntryLineRepository.findByGlAccount(glAccount);

        // Filter by date range and dimensions, sum the amounts
        BigDecimal total = BigDecimal.ZERO;

        for (JournalEntryLine line : lines) {
            JournalEntry entry = line.getJournalEntry();

            // Only include posted entries within date range
            if (entry.getStatus() != JournalEntry.EntryStatus.POSTED) {
                continue;
            }

            if (entry.getEntryDate().isBefore(startDate) || entry.getEntryDate().isAfter(endDate)) {
                continue;
            }

            // Match dimensions if specified
            if (department != null && !department.equals(line.getDepartment())) {
                continue;
            }

            if (costCenter != null && !costCenter.equals(line.getCostCenter())) {
                continue;
            }

            // Add net amount (debit - credit)
            total = total.add(line.getNetAmount());
        }

        // For expense/cost accounts, use absolute value
        if (glAccount.getAccountType() == GLAccount.AccountType.EXPENSE ||
            glAccount.getAccountType() == GLAccount.AccountType.COST_OF_SALES) {
            total = total.abs();
        }

        return total;
    }

    /**
     * Get variances for a budget
     */
    public List<BudgetVariance> getVariancesForBudget(UUID budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));
        return budgetVarianceRepository.findByBudget(budget);
    }

    /**
     * Get variances by type
     */
    public List<BudgetVariance> getVariancesByType(UUID budgetId, BudgetVariance.VarianceType varianceType) {
        List<BudgetVariance> allVariances = getVariancesForBudget(budgetId);
        return allVariances.stream()
                .filter(v -> v.getVarianceType() == varianceType)
                .toList();
    }

    /**
     * Get unfavorable variances (over budget)
     */
    public List<BudgetVariance> getUnfavorableVariances(UUID budgetId) {
        return getVariancesByType(budgetId, BudgetVariance.VarianceType.UNFAVORABLE);
    }

    /**
     * Get favorable variances (under budget)
     */
    public List<BudgetVariance> getFavorableVariances(UUID budgetId) {
        return getVariancesByType(budgetId, BudgetVariance.VarianceType.FAVORABLE);
    }

    /**
     * Get variances for a specific GL account
     */
    public List<BudgetVariance> getVariancesForAccount(UUID budgetId, UUID glAccountId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));

        GLAccount glAccount = glAccountRepository.findById(glAccountId)
                .orElseThrow(() -> new RuntimeException("GL Account not found with id: " + glAccountId));

        return budgetVarianceRepository.findByBudgetAndGlAccount(budget, glAccount);
    }

    /**
     * Get variances for a department
     */
    public List<BudgetVariance> getVariancesForDepartment(UUID budgetId, UUID departmentId) {
        List<BudgetVariance> allVariances = getVariancesForBudget(budgetId);
        return allVariances.stream()
                .filter(v -> v.getDepartment() != null && v.getDepartment().getId().equals(departmentId))
                .toList();
    }

    /**
     * Get variance summary for a budget
     */
    public Map<String, Object> getVarianceSummary(UUID budgetId) {
        List<BudgetVariance> variances = getVariancesForBudget(budgetId);

        BigDecimal totalBudgeted = variances.stream()
                .map(BudgetVariance::getBudgetedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActual = variances.stream()
                .map(BudgetVariance::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVariance = totalActual.subtract(totalBudgeted);

        long favorableCount = variances.stream()
                .filter(v -> v.getVarianceType() == BudgetVariance.VarianceType.FAVORABLE)
                .count();

        long unfavorableCount = variances.stream()
                .filter(v -> v.getVarianceType() == BudgetVariance.VarianceType.UNFAVORABLE)
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBudgeted", totalBudgeted);
        summary.put("totalActual", totalActual);
        summary.put("totalVariance", totalVariance);
        summary.put("favorableCount", favorableCount);
        summary.put("unfavorableCount", unfavorableCount);
        summary.put("totalVarianceCount", variances.size());

        if (totalBudgeted.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal variancePercentage = totalVariance
                    .divide(totalBudgeted, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            summary.put("variancePercentage", variancePercentage);
        } else {
            summary.put("variancePercentage", BigDecimal.ZERO);
        }

        return summary;
    }

    /**
     * Refresh variances for a budget (recalculate all)
     */
    @Transactional
    public List<BudgetVariance> refreshVariances(UUID budgetId) {
        log.info("Refreshing variances for budget: {}", budgetId);

        // Delete existing variances
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));

        List<BudgetVariance> existingVariances = budgetVarianceRepository.findByBudget(budget);
        budgetVarianceRepository.deleteAll(existingVariances);

        // Recalculate
        return calculateVariancesForBudget(budgetId);
    }

    /**
     * Get budget utilization percentage
     */
    public BigDecimal getBudgetUtilization(UUID budgetId) {
        Map<String, Object> summary = getVarianceSummary(budgetId);

        BigDecimal totalBudgeted = (BigDecimal) summary.get("totalBudgeted");
        BigDecimal totalActual = (BigDecimal) summary.get("totalActual");

        if (totalBudgeted.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalActual
                .divide(totalBudgeted, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
