package lt.elektromeistras.controller;

import lt.elektromeistras.domain.BudgetVariance;
import lt.elektromeistras.service.BudgetVarianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/budget-variances")
@RequiredArgsConstructor
@Slf4j
public class BudgetVarianceController {

    private final BudgetVarianceService budgetVarianceService;

    /**
     * Calculate variances for a budget
     * POST /api/budget-variances/budget/{budgetId}/calculate
     */
    @PostMapping("/budget/{budgetId}/calculate")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetVariance>> calculateVariances(@PathVariable UUID budgetId) {
        log.info("Calculating variances for budget: {}", budgetId);
        List<BudgetVariance> variances = budgetVarianceService.calculateVariancesForBudget(budgetId);
        return ResponseEntity.ok(variances);
    }

    /**
     * Get variances for a budget
     * GET /api/budget-variances/budget/{budgetId}
     */
    @GetMapping("/budget/{budgetId}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetVariance>> getVariancesForBudget(@PathVariable UUID budgetId) {
        List<BudgetVariance> variances = budgetVarianceService.getVariancesForBudget(budgetId);
        return ResponseEntity.ok(variances);
    }

    /**
     * Get unfavorable variances (over budget)
     * GET /api/budget-variances/budget/{budgetId}/unfavorable
     */
    @GetMapping("/budget/{budgetId}/unfavorable")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetVariance>> getUnfavorableVariances(@PathVariable UUID budgetId) {
        List<BudgetVariance> variances = budgetVarianceService.getUnfavorableVariances(budgetId);
        return ResponseEntity.ok(variances);
    }

    /**
     * Get favorable variances (under budget)
     * GET /api/budget-variances/budget/{budgetId}/favorable
     */
    @GetMapping("/budget/{budgetId}/favorable")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetVariance>> getFavorableVariances(@PathVariable UUID budgetId) {
        List<BudgetVariance> variances = budgetVarianceService.getFavorableVariances(budgetId);
        return ResponseEntity.ok(variances);
    }

    /**
     * Get variances for a specific GL account
     * GET /api/budget-variances/budget/{budgetId}/account/{accountId}
     */
    @GetMapping("/budget/{budgetId}/account/{accountId}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetVariance>> getVariancesForAccount(
            @PathVariable UUID budgetId,
            @PathVariable UUID accountId) {
        List<BudgetVariance> variances = budgetVarianceService.getVariancesForAccount(budgetId, accountId);
        return ResponseEntity.ok(variances);
    }

    /**
     * Get variances for a department
     * GET /api/budget-variances/budget/{budgetId}/department/{departmentId}
     */
    @GetMapping("/budget/{budgetId}/department/{departmentId}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetVariance>> getVariancesForDepartment(
            @PathVariable UUID budgetId,
            @PathVariable UUID departmentId) {
        List<BudgetVariance> variances = budgetVarianceService.getVariancesForDepartment(budgetId, departmentId);
        return ResponseEntity.ok(variances);
    }

    /**
     * Get variance summary for a budget
     * GET /api/budget-variances/budget/{budgetId}/summary
     */
    @GetMapping("/budget/{budgetId}/summary")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Map<String, Object>> getVarianceSummary(@PathVariable UUID budgetId) {
        Map<String, Object> summary = budgetVarianceService.getVarianceSummary(budgetId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Refresh variances for a budget (recalculate all)
     * POST /api/budget-variances/budget/{budgetId}/refresh
     */
    @PostMapping("/budget/{budgetId}/refresh")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetVariance>> refreshVariances(@PathVariable UUID budgetId) {
        log.info("Refreshing variances for budget: {}", budgetId);
        List<BudgetVariance> variances = budgetVarianceService.refreshVariances(budgetId);
        return ResponseEntity.ok(variances);
    }

    /**
     * Get budget utilization percentage
     * GET /api/budget-variances/budget/{budgetId}/utilization
     */
    @GetMapping("/budget/{budgetId}/utilization")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<BigDecimal> getBudgetUtilization(@PathVariable UUID budgetId) {
        BigDecimal utilization = budgetVarianceService.getBudgetUtilization(budgetId);
        return ResponseEntity.ok(utilization);
    }
}
