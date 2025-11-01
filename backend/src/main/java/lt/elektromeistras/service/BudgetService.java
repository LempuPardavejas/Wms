package lt.elektromeistras.service;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.repository.BudgetRepository;
import lt.elektromeistras.repository.BudgetLineRepository;
import lt.elektromeistras.repository.BudgetPeriodRepository;
import lt.elektromeistras.repository.GLAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing budgets and budget lines
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final BudgetPeriodRepository budgetPeriodRepository;
    private final GLAccountRepository glAccountRepository;

    /**
     * Get budget by ID
     */
    public Budget getById(UUID id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
    }

    /**
     * Get budget by code
     */
    public Budget getByCode(String code) {
        return budgetRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Budget not found with code: " + code));
    }

    /**
     * Get all budgets with pagination
     */
    public Page<Budget> getAllBudgets(Pageable pageable) {
        return budgetRepository.findAll(pageable);
    }

    /**
     * Get budgets by period
     */
    public List<Budget> getBudgetsByPeriod(UUID periodId) {
        BudgetPeriod period = budgetPeriodRepository.findById(periodId)
                .orElseThrow(() -> new RuntimeException("Budget period not found with id: " + periodId));
        return budgetRepository.findByBudgetPeriod(period);
    }

    /**
     * Get budgets by status
     */
    public List<Budget> getBudgetsByStatus(Budget.BudgetStatus status) {
        return budgetRepository.findByStatus(status);
    }

    /**
     * Create new budget
     */
    @Transactional
    public Budget createBudget(Budget budget) {
        log.info("Creating new budget: {}", budget.getCode());

        // Validate period exists
        BudgetPeriod period = budgetPeriodRepository.findById(budget.getBudgetPeriod().getId())
                .orElseThrow(() -> new RuntimeException("Budget period not found"));

        budget.setBudgetPeriod(period);
        budget.setStatus(Budget.BudgetStatus.DRAFT);
        budget.setVersion(1);

        return budgetRepository.save(budget);
    }

    /**
     * Update existing budget
     */
    @Transactional
    public Budget updateBudget(UUID id, Budget updatedBudget) {
        log.info("Updating budget: {}", id);

        Budget budget = getById(id);

        // Only allow updates if budget is in DRAFT or SUBMITTED status
        if (budget.getStatus() == Budget.BudgetStatus.APPROVED ||
            budget.getStatus() == Budget.BudgetStatus.ACTIVE) {
            throw new RuntimeException("Cannot update budget in " + budget.getStatus() + " status");
        }

        budget.setName(updatedBudget.getName());
        budget.setDescription(updatedBudget.getDescription());
        budget.setBudgetType(updatedBudget.getBudgetType());
        budget.setNotes(updatedBudget.getNotes());

        return budgetRepository.save(budget);
    }

    /**
     * Add budget line to budget
     */
    @Transactional
    public Budget addBudgetLine(UUID budgetId, BudgetLine budgetLine) {
        log.info("Adding budget line to budget: {}", budgetId);

        Budget budget = getById(budgetId);

        // Only allow adding lines if budget is in DRAFT status
        if (budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new RuntimeException("Cannot add lines to budget in " + budget.getStatus() + " status");
        }

        // Validate GL account exists
        GLAccount glAccount = glAccountRepository.findById(budgetLine.getGlAccount().getId())
                .orElseThrow(() -> new RuntimeException("GL Account not found"));

        budgetLine.setGlAccount(glAccount);
        budgetLine.setLineNumber(budget.getBudgetLines().size() + 1);

        budget.addBudgetLine(budgetLine);

        return budgetRepository.save(budget);
    }

    /**
     * Remove budget line from budget
     */
    @Transactional
    public Budget removeBudgetLine(UUID budgetId, UUID lineId) {
        log.info("Removing budget line {} from budget: {}", lineId, budgetId);

        Budget budget = getById(budgetId);

        // Only allow removing lines if budget is in DRAFT status
        if (budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new RuntimeException("Cannot remove lines from budget in " + budget.getStatus() + " status");
        }

        BudgetLine lineToRemove = budget.getBudgetLines().stream()
                .filter(line -> line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Budget line not found"));

        budget.removeBudgetLine(lineToRemove);

        return budgetRepository.save(budget);
    }

    /**
     * Submit budget for approval
     */
    @Transactional
    public Budget submitBudget(UUID budgetId) {
        log.info("Submitting budget for approval: {}", budgetId);

        Budget budget = getById(budgetId);

        if (budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT budgets can be submitted");
        }

        if (budget.getBudgetLines().isEmpty()) {
            throw new RuntimeException("Cannot submit budget with no lines");
        }

        budget.setStatus(Budget.BudgetStatus.SUBMITTED);

        return budgetRepository.save(budget);
    }

    /**
     * Approve budget
     */
    @Transactional
    public Budget approveBudget(UUID budgetId, UUID approvedByUserId) {
        log.info("Approving budget: {}", budgetId);

        Budget budget = getById(budgetId);

        if (budget.getStatus() != Budget.BudgetStatus.SUBMITTED) {
            throw new RuntimeException("Only SUBMITTED budgets can be approved");
        }

        budget.setStatus(Budget.BudgetStatus.APPROVED);
        budget.setApprovedAt(java.time.LocalDateTime.now());
        // Set approvedBy user if needed

        return budgetRepository.save(budget);
    }

    /**
     * Activate budget
     */
    @Transactional
    public Budget activateBudget(UUID budgetId) {
        log.info("Activating budget: {}", budgetId);

        Budget budget = getById(budgetId);

        if (budget.getStatus() != Budget.BudgetStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED budgets can be activated");
        }

        budget.setStatus(Budget.BudgetStatus.ACTIVE);

        return budgetRepository.save(budget);
    }

    /**
     * Reject budget
     */
    @Transactional
    public Budget rejectBudget(UUID budgetId, String rejectionReason) {
        log.info("Rejecting budget: {}", budgetId);

        Budget budget = getById(budgetId);

        if (budget.getStatus() != Budget.BudgetStatus.SUBMITTED) {
            throw new RuntimeException("Only SUBMITTED budgets can be rejected");
        }

        budget.setStatus(Budget.BudgetStatus.REJECTED);
        budget.setNotes((budget.getNotes() != null ? budget.getNotes() + "\n" : "") +
                "Rejection: " + rejectionReason);

        return budgetRepository.save(budget);
    }

    /**
     * Get budget lines by budget
     */
    public List<BudgetLine> getBudgetLines(UUID budgetId) {
        Budget budget = getById(budgetId);
        return budgetLineRepository.findByBudget(budget);
    }

    /**
     * Get total budgeted amount for a GL account across all active budgets
     */
    public BigDecimal getTotalBudgetedAmount(UUID glAccountId) {
        GLAccount glAccount = glAccountRepository.findById(glAccountId)
                .orElseThrow(() -> new RuntimeException("GL Account not found"));

        List<BudgetLine> lines = budgetLineRepository.findByGlAccount(glAccount);

        return lines.stream()
                .filter(line -> line.getBudget().getStatus() == Budget.BudgetStatus.ACTIVE)
                .map(BudgetLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Delete budget (only DRAFT budgets)
     */
    @Transactional
    public void deleteBudget(UUID budgetId) {
        log.info("Deleting budget: {}", budgetId);

        Budget budget = getById(budgetId);

        if (budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT budgets can be deleted");
        }

        budgetRepository.delete(budget);
    }
}
