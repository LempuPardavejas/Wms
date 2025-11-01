package lt.elektromeistras.controller;

import lt.elektromeistras.domain.BudgetPeriod;
import lt.elektromeistras.dto.request.CreateBudgetPeriodRequest;
import lt.elektromeistras.repository.BudgetPeriodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controller for managing budget periods
 */
@RestController
@RequestMapping("/api/budget-periods")
@RequiredArgsConstructor
@Slf4j
public class BudgetPeriodController {

    private final BudgetPeriodRepository budgetPeriodRepository;

    /**
     * Get all budget periods
     * GET /api/budget-periods
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetPeriod>> getAllPeriods() {
        List<BudgetPeriod> periods = budgetPeriodRepository.findAll();
        return ResponseEntity.ok(periods);
    }

    /**
     * Get active budget periods
     * GET /api/budget-periods/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetPeriod>> getActivePeriods() {
        List<BudgetPeriod> periods = budgetPeriodRepository.findByIsActiveTrue();
        return ResponseEntity.ok(periods);
    }

    /**
     * Get budget period by ID
     * GET /api/budget-periods/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<BudgetPeriod> getById(@PathVariable UUID id) {
        BudgetPeriod period = budgetPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget period not found with id: " + id));
        return ResponseEntity.ok(period);
    }

    /**
     * Get budget period by code
     * GET /api/budget-periods/code/{code}
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<BudgetPeriod> getByCode(@PathVariable String code) {
        BudgetPeriod period = budgetPeriodRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Budget period not found with code: " + code));
        return ResponseEntity.ok(period);
    }

    /**
     * Get budget periods by fiscal year
     * GET /api/budget-periods/fiscal-year/{year}
     */
    @GetMapping("/fiscal-year/{year}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetPeriod>> getByFiscalYear(@PathVariable Integer year) {
        List<BudgetPeriod> periods = budgetPeriodRepository.findByFiscalYear(year);
        return ResponseEntity.ok(periods);
    }

    /**
     * Get budget periods by period type
     * GET /api/budget-periods/type/{periodType}
     */
    @GetMapping("/type/{periodType}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetPeriod>> getByPeriodType(@PathVariable String periodType) {
        BudgetPeriod.PeriodType type = BudgetPeriod.PeriodType.valueOf(periodType);
        List<BudgetPeriod> periods = budgetPeriodRepository.findByPeriodType(type);
        return ResponseEntity.ok(periods);
    }

    /**
     * Get budget periods by status
     * GET /api/budget-periods/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetPeriod>> getByStatus(@PathVariable String status) {
        BudgetPeriod.PeriodStatus periodStatus = BudgetPeriod.PeriodStatus.valueOf(status);
        List<BudgetPeriod> periods = budgetPeriodRepository.findByStatus(periodStatus);
        return ResponseEntity.ok(periods);
    }

    /**
     * Get budget period by date
     * GET /api/budget-periods/by-date?date=2025-06-15
     */
    @GetMapping("/by-date")
    @PreAuthorize("hasAnyAuthority('BUDGET_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BudgetPeriod>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BudgetPeriod> periods = budgetPeriodRepository.findByDate(date);
        return ResponseEntity.ok(periods);
    }

    /**
     * Create new budget period
     * POST /api/budget-periods
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('BUDGET_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<BudgetPeriod> createPeriod(@Valid @RequestBody CreateBudgetPeriodRequest request) {
        log.info("Creating new budget period: {}", request.getCode());

        if (budgetPeriodRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Budget period with code " + request.getCode() + " already exists");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        BudgetPeriod period = BudgetPeriod.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .periodType(BudgetPeriod.PeriodType.valueOf(request.getPeriodType()))
                .fiscalYear(request.getFiscalYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(BudgetPeriod.PeriodStatus.DRAFT)
                .isActive(true)
                .build();

        BudgetPeriod created = budgetPeriodRepository.save(period);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update budget period
     * PUT /api/budget-periods/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BUDGET_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<BudgetPeriod> updatePeriod(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBudgetPeriodRequest request) {

        log.info("Updating budget period: {}", id);

        BudgetPeriod period = budgetPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget period not found"));

        // Only allow updates if period is in DRAFT status
        if (period.getStatus() != BudgetPeriod.PeriodStatus.DRAFT) {
            throw new RuntimeException("Cannot update period in " + period.getStatus() + " status");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        period.setName(request.getName());
        period.setDescription(request.getDescription());
        period.setPeriodType(BudgetPeriod.PeriodType.valueOf(request.getPeriodType()));
        period.setFiscalYear(request.getFiscalYear());
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());

        BudgetPeriod updated = budgetPeriodRepository.save(period);
        return ResponseEntity.ok(updated);
    }

    /**
     * Activate budget period
     * POST /api/budget-periods/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('BUDGET_APPROVE', 'ADMIN_FULL')")
    public ResponseEntity<BudgetPeriod> activatePeriod(@PathVariable UUID id) {
        log.info("Activating budget period: {}", id);

        BudgetPeriod period = budgetPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget period not found"));

        period.setStatus(BudgetPeriod.PeriodStatus.ACTIVE);
        BudgetPeriod updated = budgetPeriodRepository.save(period);

        return ResponseEntity.ok(updated);
    }

    /**
     * Close budget period
     * POST /api/budget-periods/{id}/close
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('BUDGET_APPROVE', 'ADMIN_FULL')")
    public ResponseEntity<BudgetPeriod> closePeriod(@PathVariable UUID id) {
        log.info("Closing budget period: {}", id);

        BudgetPeriod period = budgetPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget period not found"));

        if (period.getStatus() != BudgetPeriod.PeriodStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE periods can be closed");
        }

        period.setStatus(BudgetPeriod.PeriodStatus.CLOSED);
        BudgetPeriod updated = budgetPeriodRepository.save(period);

        return ResponseEntity.ok(updated);
    }

    /**
     * Archive budget period
     * POST /api/budget-periods/{id}/archive
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyAuthority('BUDGET_APPROVE', 'ADMIN_FULL')")
    public ResponseEntity<BudgetPeriod> archivePeriod(@PathVariable UUID id) {
        log.info("Archiving budget period: {}", id);

        BudgetPeriod period = budgetPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget period not found"));

        if (period.getStatus() != BudgetPeriod.PeriodStatus.CLOSED) {
            throw new RuntimeException("Only CLOSED periods can be archived");
        }

        period.setStatus(BudgetPeriod.PeriodStatus.ARCHIVED);
        BudgetPeriod updated = budgetPeriodRepository.save(period);

        return ResponseEntity.ok(updated);
    }

    /**
     * Delete budget period
     * DELETE /api/budget-periods/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BUDGET_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deletePeriod(@PathVariable UUID id) {
        log.info("Deleting budget period: {}", id);

        BudgetPeriod period = budgetPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget period not found"));

        // Only allow deletion if period is in DRAFT status
        if (period.getStatus() != BudgetPeriod.PeriodStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT periods can be deleted");
        }

        budgetPeriodRepository.delete(period);
        return ResponseEntity.noContent().build();
    }
}
