package lt.elektromeistras.service;

import lt.elektromeistras.domain.Customer;
import lt.elektromeistras.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * FAST customer search for autocomplete - CRITICAL for UX
     * Limits results to top 20 for performance
     * Searches by code, company name, first/last name, email, phone, VAT
     * Prioritizes exact code matches
     */
    public List<Customer> searchCustomers(String query) {
        log.debug("Searching customers with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            // Return top 20 active customers if no query
            return customerRepository.findByIsActiveTrue(
                    PageRequest.of(0, 20, Sort.by("code"))
            ).getContent();
        }

        // Use repository search which has optimized ordering
        List<Customer> results = customerRepository.searchCustomers(query.trim());

        // Limit to 20 for autocomplete performance
        return results.size() > 20 ? results.subList(0, 20) : results;
    }

    /**
     * ULTRA FAST exact code lookup
     * Uses indexed code column for instant lookup
     */
    public Customer getByCode(String code) {
        log.debug("Getting customer by code: {}", code);
        return customerRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Customer not found with code: " + code));
    }

    /**
     * Fast code prefix search
     */
    public List<Customer> searchByCodePrefix(String codePrefix) {
        log.debug("Searching customers by code prefix: {}", codePrefix);
        List<Customer> results = customerRepository.findByCodeStartingWith(codePrefix);
        return results.size() > 20 ? results.subList(0, 20) : results;
    }

    /**
     * Get customer by ID
     */
    public Customer getById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    /**
     * Get customer by email
     */
    public Customer getByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
    }

    /**
     * Get all customers with pagination
     */
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findByIsActiveTrue(pageable);
    }

    /**
     * Search customers with pagination
     */
    public Page<Customer> searchCustomers(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return customerRepository.findByIsActiveTrue(pageable);
        }
        return customerRepository.searchCustomers(query.trim(), pageable);
    }

    /**
     * Get customers by type
     */
    public Page<Customer> getCustomersByType(Customer.CustomerType customerType, Pageable pageable) {
        return customerRepository.findByCustomerTypeAndIsActiveTrue(customerType, pageable);
    }

    /**
     * Get customers over credit limit
     */
    public List<Customer> getCustomersOverCreditLimit() {
        return customerRepository.findCustomersOverCreditLimit();
    }

    /**
     * Create new customer with auto-generated code
     */
    @Transactional
    public Customer createCustomer(Customer customer) {
        log.info("Creating new customer");

        // Auto-generate code if not provided
        if (customer.getCode() == null || customer.getCode().trim().isEmpty()) {
            customer.setCode(generateCustomerCode(customer.getCustomerType()));
        }

        // Validate unique code
        if (customerRepository.findByCode(customer.getCode()).isPresent()) {
            throw new RuntimeException("Customer with code already exists: " + customer.getCode());
        }

        // Validate unique email if provided
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
                throw new RuntimeException("Customer with email already exists: " + customer.getEmail());
            }
        }

        return customerRepository.save(customer);
    }

    /**
     * Update customer
     */
    @Transactional
    public Customer updateCustomer(UUID id, Customer customerDetails) {
        log.info("Updating customer with id: {}", id);

        Customer customer = getById(id);

        // Update fields
        customer.setCustomerType(customerDetails.getCustomerType());
        customer.setCompanyName(customerDetails.getCompanyName());
        customer.setVatCode(customerDetails.getVatCode());
        customer.setCompanyCode(customerDetails.getCompanyCode());
        customer.setFirstName(customerDetails.getFirstName());
        customer.setLastName(customerDetails.getLastName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setMobile(customerDetails.getMobile());
        customer.setAddress(customerDetails.getAddress());
        customer.setCity(customerDetails.getCity());
        customer.setPostalCode(customerDetails.getPostalCode());
        customer.setCountry(customerDetails.getCountry());
        customer.setPriceGroupId(customerDetails.getPriceGroupId());
        customer.setCreditLimit(customerDetails.getCreditLimit());
        customer.setPaymentTermsDays(customerDetails.getPaymentTermsDays());
        customer.setIsActive(customerDetails.getIsActive());
        customer.setNotes(customerDetails.getNotes());

        return customerRepository.save(customer);
    }

    /**
     * Delete customer (soft delete by setting isActive = false)
     */
    @Transactional
    public void deleteCustomer(UUID id) {
        log.info("Deleting customer with id: {}", id);
        Customer customer = getById(id);
        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    /**
     * Generate customer code based on type
     * B001, B002 for BUSINESS
     * C001, C002 for CONTRACTOR
     * R001, R002 for RETAIL
     */
    private String generateCustomerCode(Customer.CustomerType type) {
        String prefix = switch (type) {
            case BUSINESS -> "B";
            case CONTRACTOR -> "C";
            case RETAIL -> "R";
        };

        // Find highest number for this prefix
        List<Customer> existingCustomers = customerRepository.findByCodeStartingWith(prefix);
        int maxNumber = 0;

        for (Customer c : existingCustomers) {
            String code = c.getCode();
            if (code.length() > 1) {
                try {
                    int number = Integer.parseInt(code.substring(1));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // Ignore codes that don't follow the pattern
                }
            }
        }

        return String.format("%s%03d", prefix, maxNumber + 1);
    }

    /**
     * Check if customer code exists
     */
    public boolean existsByCode(String code) {
        return customerRepository.findByCode(code).isPresent();
    }
}
