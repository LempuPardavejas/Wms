package lt.elektromeistras.controller;

import lt.elektromeistras.domain.Customer;
import lt.elektromeistras.dto.response.CustomerSearchResponse;
import lt.elektromeistras.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * FAST customer search for autocomplete - CRITICAL endpoint
     * GET /api/customers/search?q=elektros
     * Returns top 20 results optimized for autocomplete
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<CustomerSearchResponse>> searchCustomers(@RequestParam String q) {
        log.debug("Customer search request: {}", q);

        List<Customer> customers = customerService.searchCustomers(q);

        List<CustomerSearchResponse> response = customers.stream()
                .map(this::mapToSearchResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get customer by code - for exact code lookup
     * GET /api/customers/code/B001
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Customer> getByCode(@PathVariable String code) {
        Customer customer = customerService.getByCode(code);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get customer by ID
     * GET /api/customers/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Customer> getById(@PathVariable UUID id) {
        Customer customer = customerService.getById(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get all customers with pagination
     * GET /api/customers?page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Customer>> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }

    /**
     * Get customers by type
     * GET /api/customers/type/BUSINESS
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Customer>> getCustomersByType(
            @PathVariable Customer.CustomerType type,
            Pageable pageable) {
        Page<Customer> customers = customerService.getCustomersByType(type, pageable);
        return ResponseEntity.ok(customers);
    }

    /**
     * Create new customer
     * POST /api/customers
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer created = customerService.createCustomer(customer);
        return ResponseEntity.ok(created);
    }

    /**
     * Update customer
     * PUT /api/customers/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable UUID id,
            @RequestBody Customer customer) {
        Customer updated = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete customer (soft delete)
     * DELETE /api/customers/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Map Customer to lightweight CustomerSearchResponse
     */
    private CustomerSearchResponse mapToSearchResponse(Customer c) {
        return new CustomerSearchResponse(
                c.getId(),
                c.getCode(),
                c.getCustomerType().name(),
                c.getCompanyName(),
                c.getFirstName(),
                c.getLastName(),
                c.getEmail(),
                c.getPhone(),
                c.getCity(),
                c.getCreditLimit(),
                c.getCurrentBalance()
        );
    }
}
