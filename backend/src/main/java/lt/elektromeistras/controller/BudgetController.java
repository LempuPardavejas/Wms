package lt.elektromeistras.controller;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.dto.request.BudgetLineRequest;
import lt.elektromeistras.dto.request.CreateBudgetRequest;
import lt.elektromeistras.dto.response.BudgetResponse;
import lt.elektromeistras.dto.response.BudgetLineResponse;
import lt.elektromeistras.service.BudgetService;
import lt.elektromeistras.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;
    private final GLAccountRepository glAccountRepository;
    private final DepartmentRepository departmentRepository;
    private final CostCenterRepository costCenterRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final SeriesRepository seriesRepository;
    private final PersonRepository personRepository;
    private final DimensionValueRepository dimensionValueRepository;

    /**
     * Get all budgets with pagination
     * GET /api/budgets?page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Budget>> getAllBudgets(Pageable pageable) {
        Page<Budget> budgets = budgetService.getAllBudgets(pageable);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budget by ID
     * GET /api/budgets/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Budget> getById(@PathVariable UUID id) {
        Budget budget = budgetService.getById(id);
        return ResponseEntity.ok(budget);
    }

    /**
     * Get budget by code
     * GET /api/budgets/code/{code}
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Budget> getByCode(@PathVariable String code) {
        Budget budget = budgetService.getByCode(code);
        return ResponseEntity.ok(budget);
    }

    /**
     * Get budgets by period
     * GET /api/budgets/period/{periodId}
     */
    @GetMapping("/period/{periodId}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Budget>> getBudgetsByPeriod(@PathVariable UUID periodId) {
        List<Budget> budgets = budgetService.getBudgetsByPeriod(periodId);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Get budgets by status
     * GET /api/budgets/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Budget>> getBudgetsByStatus(@PathVariable String status) {
        Budget.BudgetStatus budgetStatus = Budget.BudgetStatus.valueOf(status);
        List<Budget> budgets = budgetService.getBudgetsByStatus(budgetStatus);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Create new budget
     * POST /api/budgets
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('BUDGET_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        log.info("Creating new budget: {}", request.getCode());

        // Build budget from request
        Budget budget = Budget.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .budgetType(Budget.BudgetType.valueOf(request.getBudgetType()))
                .notes(request.getNotes())
                .build();

        // Create budget without lines first
        Budget createdBudget = budgetService.createBudget(budget);

        // Add budget lines
        for (BudgetLineRequest lineRequest : request.getLines()) {
            BudgetLine line = mapToBudgetLine(lineRequest);
            budgetService.addBudgetLine(createdBudget.getId(), line);
        }

        // Fetch updated budget with lines
        Budget finalBudget = budgetService.getById(createdBudget.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(finalBudget);
    }

    /**
     * Update budget
     * PUT /api/budgets/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BUDGET_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBudgetRequest request) {
        log.info("Updating budget: {}", id);

        Budget budget = Budget.builder()
                .name(request.getName())
                .description(request.getDescription())
                .budgetType(Budget.BudgetType.valueOf(request.getBudgetType()))
                .notes(request.getNotes())
                .build();

        Budget updatedBudget = budgetService.updateBudget(id, budget);
        return ResponseEntity.ok(updatedBudget);
    }

    /**
     * Add budget line
     * POST /api/budgets/{id}/lines
     */
    @PostMapping("/{id}/lines")
    @PreAuthorize("hasAnyAuthority('BUDGET_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Budget> addBudgetLine(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetLineRequest request) {
        log.info("Adding budget line to budget: {}", id);

        BudgetLine line = mapToBudgetLine(request);
        Budget budget = budgetService.addBudgetLine(id, line);

        return ResponseEntity.ok(budget);
    }

    /**
     * Remove budget line
     * DELETE /api/budgets/{id}/lines/{lineId}
     */
    @DeleteMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAnyAuthority('BUDGET_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Budget> removeBudgetLine(
            @PathVariable UUID id,
            @PathVariable UUID lineId) {
        log.info("Removing budget line {} from budget: {}", lineId, id);

        Budget budget = budgetService.removeBudgetLine(id, lineId);
        return ResponseEntity.ok(budget);
    }

    /**
     * Submit budget for approval
     * POST /api/budgets/{id}/submit
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('BUDGET_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Budget> submitBudget(@PathVariable UUID id) {
        log.info("Submitting budget: {}", id);
        Budget budget = budgetService.submitBudget(id);
        return ResponseEntity.ok(budget);
    }

    /**
     * Approve budget
     * POST /api/budgets/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('BUDGET_APPROVE', 'ADMIN_FULL')")
    public ResponseEntity<Budget> approveBudget(@PathVariable UUID id) {
        log.info("Approving budget: {}", id);
        // TODO: Get current user ID from security context
        Budget budget = budgetService.approveBudget(id, null);
        return ResponseEntity.ok(budget);
    }

    /**
     * Activate budget
     * POST /api/budgets/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('BUDGET_APPROVE', 'ADMIN_FULL')")
    public ResponseEntity<Budget> activateBudget(@PathVariable UUID id) {
        log.info("Activating budget: {}", id);
        Budget budget = budgetService.activateBudget(id);
        return ResponseEntity.ok(budget);
    }

    /**
     * Reject budget
     * POST /api/budgets/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('BUDGET_APPROVE', 'ADMIN_FULL')")
    public ResponseEntity<Budget> rejectBudget(
            @PathVariable UUID id,
            @RequestParam String reason) {
        log.info("Rejecting budget: {}", id);
        Budget budget = budgetService.rejectBudget(id, reason);
        return ResponseEntity.ok(budget);
    }

    /**
     * Delete budget
     * DELETE /api/budgets/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BUDGET_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID id) {
        log.info("Deleting budget: {}", id);
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get budget lines
     * GET /api/budgets/{id}/lines
     */
    @GetMapping("/{id}/lines")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetLine>> getBudgetLines(@PathVariable UUID id) {
        List<BudgetLine> lines = budgetService.getBudgetLines(id);
        return ResponseEntity.ok(lines);
    }

    // Helper method to map request to BudgetLine entity
    private BudgetLine mapToBudgetLine(BudgetLineRequest request) {
        BudgetLine line = BudgetLine.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .notes(request.getNotes())
                .build();

        // Set GL account
        GLAccount glAccount = glAccountRepository.findById(request.getGlAccountId())
                .orElseThrow(() -> new RuntimeException("GL Account not found"));
        line.setGlAccount(glAccount);

        // Set static dimensions
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            line.setDepartment(department);
        }

        if (request.getCostCenterId() != null) {
            CostCenter costCenter = costCenterRepository.findById(request.getCostCenterId())
                    .orElseThrow(() -> new RuntimeException("Cost Center not found"));
            line.setCostCenter(costCenter);
        }

        if (request.getBusinessObjectId() != null) {
            BusinessObject businessObject = businessObjectRepository.findById(request.getBusinessObjectId())
                    .orElseThrow(() -> new RuntimeException("Business Object not found"));
            line.setBusinessObject(businessObject);
        }

        if (request.getSeriesId() != null) {
            Series series = seriesRepository.findById(request.getSeriesId())
                    .orElseThrow(() -> new RuntimeException("Series not found"));
            line.setSeries(series);
        }

        if (request.getPersonId() != null) {
            Person person = personRepository.findById(request.getPersonId())
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            line.setPerson(person);
        }

        // Set dynamic dimensions (example for first 5)
        if (request.getDimension1Id() != null) {
            DimensionValue dim1 = dimensionValueRepository.findById(request.getDimension1Id())
                    .orElseThrow(() -> new RuntimeException("Dimension value 1 not found"));
            line.setDimension1(dim1);
        }

        // Add similar logic for other dimensions...

        return line;
    }
}
