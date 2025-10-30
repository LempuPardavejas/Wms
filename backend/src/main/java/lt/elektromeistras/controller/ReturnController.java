package lt.elektromeistras.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lt.elektromeistras.domain.Return;
import lt.elektromeistras.dto.request.*;
import lt.elektromeistras.dto.response.*;
import lt.elektromeistras.service.ReturnService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    /**
     * Get all returns (paginated)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<Page<ReturnResponse>> getReturns(Pageable pageable) {
        return ResponseEntity.ok(returnService.getReturns(pageable));
    }

    /**
     * Get return by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<ReturnResponse> getReturn(@PathVariable UUID id) {
        return ResponseEntity.ok(returnService.getReturn(id));
    }

    /**
     * Get return by return number
     */
    @GetMapping("/number/{returnNumber}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<ReturnResponse> getReturnByNumber(@PathVariable String returnNumber) {
        return ResponseEntity.ok(returnService.getReturnByNumber(returnNumber));
    }

    /**
     * Get returns by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<Page<ReturnResponse>> getReturnsByStatus(
            @PathVariable String status,
            Pageable pageable) {
        Return.ReturnStatus returnStatus = Return.ReturnStatus.valueOf(status);
        return ResponseEntity.ok(returnService.getReturnsByStatus(returnStatus, pageable));
    }

    /**
     * Get returns by customer
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<Page<ReturnResponse>> getReturnsByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {
        return ResponseEntity.ok(returnService.getReturnsByCustomer(customerId, pageable));
    }

    /**
     * Create a new return
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<ReturnResponse> createReturn(@Valid @RequestBody CreateReturnRequest request) {
        ReturnResponse created = returnService.createReturn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Approve a return
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<ReturnResponse> approveReturn(
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateReturnStatusRequest request) {
        String notes = request != null ? request.getNotes() : null;
        ReturnResponse approved = returnService.approveReturn(id, notes);
        return ResponseEntity.ok(approved);
    }

    /**
     * Reject a return
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<ReturnResponse> rejectReturn(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReturnStatusRequest request) {
        ReturnResponse rejected = returnService.rejectReturn(id, request.getRejectionReason());
        return ResponseEntity.ok(rejected);
    }

    /**
     * Mark return as received at warehouse
     */
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('INVENTORY_MANAGE')")
    public ResponseEntity<ReturnResponse> markAsReceived(@PathVariable UUID id) {
        ReturnResponse received = returnService.markAsReceived(id);
        return ResponseEntity.ok(received);
    }

    /**
     * Inspect return items
     */
    @PostMapping("/{id}/inspect")
    @PreAuthorize("hasAuthority('INVENTORY_MANAGE')")
    public ResponseEntity<ReturnResponse> inspectReturn(
            @PathVariable UUID id,
            @Valid @RequestBody List<InspectReturnLineRequest> inspections) {
        ReturnResponse inspected = returnService.inspectReturn(id, inspections);
        return ResponseEntity.ok(inspected);
    }

    /**
     * Restock items from return
     */
    @PostMapping("/{id}/restock")
    @PreAuthorize("hasAuthority('INVENTORY_MANAGE')")
    public ResponseEntity<ReturnResponse> restockReturn(@PathVariable UUID id) {
        ReturnResponse restocked = returnService.restockReturn(id);
        return ResponseEntity.ok(restocked);
    }

    /**
     * Process refund for return
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<ReturnResponse> processRefund(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessRefundRequest request) {
        ReturnResponse refunded = returnService.processRefund(id, request);
        return ResponseEntity.ok(refunded);
    }

    /**
     * Get active return reasons
     */
    @GetMapping("/reasons")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<List<ReturnReasonResponse>> getReturnReasons() {
        return ResponseEntity.ok(returnService.getActiveReturnReasons());
    }
}
