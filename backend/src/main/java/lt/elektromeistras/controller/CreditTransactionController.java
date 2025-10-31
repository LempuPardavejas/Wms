package lt.elektromeistras.controller;

import lt.elektromeistras.dto.request.*;
import lt.elektromeistras.dto.response.*;
import lt.elektromeistras.service.CreditTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for credit transaction management
 * Handles customer credit pickups and returns
 */
@RestController
@RequestMapping("/api/credit-transactions")
@RequiredArgsConstructor
@Slf4j
public class CreditTransactionController {

    private final CreditTransactionService creditTransactionService;

    /**
     * ULTRA FAST credit pickup - optimized for rapid entry
     * POST /api/credit-transactions/quick-pickup
     */
    @PostMapping("/quick-pickup")
    @PreAuthorize("hasAnyAuthority('CREDIT_MANAGE', 'SALES_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<CreditTransactionResponse> createQuickPickup(
            @RequestBody QuickCreditPickupRequest request) {
        log.info("Quick credit pickup request for customer: {}", request.getCustomerCode());
        CreditTransactionResponse response = creditTransactionService.createQuickCreditPickup(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Create credit transaction (pickup or return)
     * POST /api/credit-transactions
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CREDIT_MANAGE', 'SALES_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<CreditTransactionResponse> createTransaction(
            @RequestBody CreateCreditTransactionRequest request) {
        log.info("Creating credit transaction for customer: {}", request.getCustomerId());
        CreditTransactionResponse response = creditTransactionService.createTransaction(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm credit transaction
     * POST /api/credit-transactions/{id}/confirm
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('CREDIT_MANAGE', 'SALES_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<CreditTransactionResponse> confirmTransaction(
            @PathVariable UUID id,
            @RequestBody ConfirmCreditTransactionRequest request) {
        log.info("Confirming credit transaction: {}", id);
        CreditTransactionResponse response = creditTransactionService.confirmTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel credit transaction
     * POST /api/credit-transactions/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('CREDIT_MANAGE', 'SALES_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<Void> cancelTransaction(
            @PathVariable UUID id,
            @RequestParam String reason) {
        log.info("Cancelling credit transaction: {}", id);
        creditTransactionService.cancelTransaction(id, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * Get transaction by ID
     * GET /api/credit-transactions/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<CreditTransactionResponse> getTransactionById(@PathVariable UUID id) {
        log.debug("Getting credit transaction: {}", id);
        CreditTransactionResponse response = creditTransactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction by transaction number
     * GET /api/credit-transactions/number/{transactionNumber}
     */
    @GetMapping("/number/{transactionNumber}")
    @PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<CreditTransactionResponse> getTransactionByNumber(
            @PathVariable String transactionNumber) {
        log.debug("Getting credit transaction by number: {}", transactionNumber);
        CreditTransactionResponse response = creditTransactionService.getTransactionByNumber(transactionNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions for a customer
     * GET /api/credit-transactions/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<CreditTransactionSummaryResponse>> getCustomerTransactions(
            @PathVariable UUID customerId,
            Pageable pageable) {
        log.debug("Getting credit transactions for customer: {}", customerId);
        Page<CreditTransactionSummaryResponse> response = creditTransactionService.getCustomerTransactions(
                customerId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent transactions for a customer
     * GET /api/credit-transactions/customer/{customerId}/recent?limit=10
     */
    @GetMapping("/customer/{customerId}/recent")
    @PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<CreditTransactionSummaryResponse>> getRecentCustomerTransactions(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Getting recent credit transactions for customer: {}", customerId);
        List<CreditTransactionSummaryResponse> response = creditTransactionService
                .getRecentCustomerTransactions(customerId, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending transactions for a customer
     * GET /api/credit-transactions/customer/{customerId}/pending
     */
    @GetMapping("/customer/{customerId}/pending")
    @PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<CreditTransactionResponse>> getPendingCustomerTransactions(
            @PathVariable UUID customerId) {
        log.debug("Getting pending credit transactions for customer: {}", customerId);
        List<CreditTransactionResponse> response = creditTransactionService
                .getPendingCustomerTransactions(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions
     * GET /api/credit-transactions?page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<CreditTransactionSummaryResponse>> getAllTransactions(Pageable pageable) {
        log.debug("Getting all credit transactions");
        Page<CreditTransactionSummaryResponse> response = creditTransactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Search transactions
     * GET /api/credit-transactions/search?q=elektros
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<CreditTransactionSummaryResponse>> searchTransactions(
            @RequestParam String q,
            Pageable pageable) {
        log.debug("Searching credit transactions: {}", q);
        Page<CreditTransactionSummaryResponse> response = creditTransactionService.searchTransactions(q, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly statement for customer
     * GET /api/credit-transactions/customer/{customerId}/statement/{year}/{month}
     */
    @GetMapping("/customer/{customerId}/statement/{year}/{month}")
    @PreAuthorize("hasAnyAuthority('CREDIT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<CreditTransactionResponse>> getMonthlyStatement(
            @PathVariable UUID customerId,
            @PathVariable int year,
            @PathVariable int month) {
        log.info("Getting monthly statement for customer: {} - {}/{}", customerId, year, month);
        List<CreditTransactionResponse> response = creditTransactionService.getMonthlyStatement(
                customerId, year, month);
        return ResponseEntity.ok(response);
    }
}
