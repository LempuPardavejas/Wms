package lt.elektromeistras.service;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.dto.request.*;
import lt.elektromeistras.dto.response.*;
import lt.elektromeistras.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CreditTransactionService {

    private final CreditTransactionRepository transactionRepository;
    private final CreditTransactionLineRepository lineRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    /**
     * ULTRA FAST credit pickup - optimized for rapid entry
     * Uses customer code and product codes for instant lookup
     */
    @Transactional
    public CreditTransactionResponse createQuickCreditPickup(QuickCreditPickupRequest request) {
        log.info("Creating quick credit pickup for customer: {}", request.getCustomerCode());

        // 1. Find customer by code (FAST - indexed lookup)
        Customer customer = customerRepository.findByCode(request.getCustomerCode())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerCode()));

        // 2. Create transaction
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCustomer(customer);
        transaction.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        transaction.setStatus(CreditTransaction.TransactionStatus.PENDING);
        transaction.setPerformedBy(request.getPerformedBy());
        transaction.setPerformedByRole(CreditTransaction.PerformedByRole.valueOf(request.getPerformedByRole()));
        transaction.setNotes(request.getNotes());

        // 3. Add line items
        for (QuickCreditPickupRequest.QuickCreditLineItem item : request.getItems()) {
            Product product = productRepository.findByCode(item.getProductCode())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductCode()));

            CreditTransactionLine line = new CreditTransactionLine();
            line.setProduct(product);
            line.setProductCode(product.getCode());
            line.setProductName(product.getName());
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(product.getBasePrice());
            line.setNotes(item.getNotes());

            transaction.addLine(line);
        }

        // 4. Save transaction
        CreditTransaction saved = transactionRepository.save(transaction);

        log.info("Quick credit pickup created: {} with {} items",
                saved.getTransactionNumber(), saved.getTotalItems());

        return mapToResponse(saved);
    }

    /**
     * Create credit transaction (pickup or return)
     */
    @Transactional
    public CreditTransactionResponse createTransaction(CreateCreditTransactionRequest request) {
        log.info("Creating credit transaction for customer: {}", request.getCustomerId());

        // 1. Find customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        // 2. Create transaction
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCustomer(customer);
        transaction.setTransactionType(CreditTransaction.TransactionType.valueOf(request.getTransactionType()));
        transaction.setStatus(CreditTransaction.TransactionStatus.PENDING);
        transaction.setPerformedBy(request.getPerformedBy());
        transaction.setPerformedByRole(CreditTransaction.PerformedByRole.valueOf(request.getPerformedByRole()));
        transaction.setNotes(request.getNotes());

        // 3. Add line items
        for (CreditTransactionLineRequest lineReq : request.getLines()) {
            UUID productId = lineReq.getProductId();
            if (productId == null && lineReq.getProductCode() != null) {
                Product product = productRepository.findByCode(lineReq.getProductCode())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + lineReq.getProductCode()));
                productId = product.getId();
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            CreditTransactionLine line = new CreditTransactionLine();
            line.setProduct(product);
            line.setProductCode(product.getCode());
            line.setProductName(product.getName());
            line.setQuantity(lineReq.getQuantity());
            line.setUnitPrice(product.getBasePrice());
            line.setNotes(lineReq.getNotes());

            transaction.addLine(line);
        }

        // 4. Save transaction
        CreditTransaction saved = transactionRepository.save(transaction);

        log.info("Credit transaction created: {} with {} items",
                saved.getTransactionNumber(), saved.getTotalItems());

        return mapToResponse(saved);
    }

    /**
     * Confirm credit transaction and update customer balance
     */
    @Transactional
    public CreditTransactionResponse confirmTransaction(UUID transactionId, ConfirmCreditTransactionRequest request) {
        log.info("Confirming credit transaction: {}", transactionId);

        CreditTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != CreditTransaction.TransactionStatus.PENDING) {
            throw new RuntimeException("Only pending transactions can be confirmed");
        }

        // Confirm transaction
        transaction.confirm(request.getConfirmedBy());
        if (request.getSignatureData() != null) {
            transaction.setSignatureData(request.getSignatureData());
        }
        if (request.getPhotoData() != null) {
            transaction.setPhotoData(request.getPhotoData());
        }
        if (request.getNotes() != null) {
            transaction.setNotes(transaction.getNotes() + "\n" + request.getNotes());
        }

        // Update customer balance
        Customer customer = transaction.getCustomer();
        if (transaction.getTransactionType() == CreditTransaction.TransactionType.PICKUP) {
            // Pickup increases balance (debt)
            customer.setCurrentBalance(customer.getCurrentBalance().add(transaction.getTotalAmount()));
        } else {
            // Return decreases balance (payment)
            customer.setCurrentBalance(customer.getCurrentBalance().subtract(transaction.getTotalAmount()));
        }
        customerRepository.save(customer);

        CreditTransaction saved = transactionRepository.save(transaction);

        log.info("Credit transaction confirmed: {} - Customer balance updated: {}",
                saved.getTransactionNumber(), customer.getCurrentBalance());

        return mapToResponse(saved);
    }

    /**
     * Cancel transaction
     */
    @Transactional
    public void cancelTransaction(UUID transactionId, String reason) {
        log.info("Cancelling credit transaction: {}", transactionId);

        CreditTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        if (transaction.getStatus() == CreditTransaction.TransactionStatus.INVOICED) {
            throw new RuntimeException("Cannot cancel invoiced transactions");
        }

        // If already confirmed, reverse customer balance
        if (transaction.getStatus() == CreditTransaction.TransactionStatus.CONFIRMED) {
            Customer customer = transaction.getCustomer();
            if (transaction.getTransactionType() == CreditTransaction.TransactionType.PICKUP) {
                customer.setCurrentBalance(customer.getCurrentBalance().subtract(transaction.getTotalAmount()));
            } else {
                customer.setCurrentBalance(customer.getCurrentBalance().add(transaction.getTotalAmount()));
            }
            customerRepository.save(customer);
        }

        transaction.setStatus(CreditTransaction.TransactionStatus.CANCELLED);
        transaction.setNotes(transaction.getNotes() + "\nCANCELLED: " + reason);
        transactionRepository.save(transaction);

        log.info("Credit transaction cancelled: {}", transaction.getTransactionNumber());
    }

    /**
     * Get transaction by ID
     */
    public CreditTransactionResponse getTransactionById(UUID id) {
        CreditTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return mapToResponse(transaction);
    }

    /**
     * Get transaction by transaction number
     */
    public CreditTransactionResponse getTransactionByNumber(String transactionNumber) {
        CreditTransaction transaction = transactionRepository.findByTransactionNumber(transactionNumber)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionNumber));
        return mapToResponse(transaction);
    }

    /**
     * Get all transactions for a customer
     */
    public Page<CreditTransactionSummaryResponse> getCustomerTransactions(UUID customerId, Pageable pageable) {
        Page<CreditTransaction> transactions = transactionRepository.findByCustomerId(customerId, pageable);
        return transactions.map(this::mapToSummaryResponse);
    }

    /**
     * Get recent transactions for a customer
     */
    public List<CreditTransactionSummaryResponse> getRecentCustomerTransactions(UUID customerId, int limit) {
        List<CreditTransaction> transactions = transactionRepository.findRecentByCustomer(
                customerId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return transactions.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending transactions for a customer
     */
    public List<CreditTransactionResponse> getPendingCustomerTransactions(UUID customerId) {
        List<CreditTransaction> transactions = transactionRepository.findPendingByCustomer(customerId);
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search transactions
     */
    public Page<CreditTransactionSummaryResponse> searchTransactions(String query, Pageable pageable) {
        Page<CreditTransaction> transactions = transactionRepository.searchTransactions(query, pageable);
        return transactions.map(this::mapToSummaryResponse);
    }

    /**
     * Get all transactions
     */
    public Page<CreditTransactionSummaryResponse> getAllTransactions(Pageable pageable) {
        Page<CreditTransaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(this::mapToSummaryResponse);
    }

    /**
     * Get monthly statement for customer
     * Returns all confirmed transactions for the specified month
     */
    public List<CreditTransactionResponse> getMonthlyStatement(UUID customerId, int year, int month) {
        log.info("Generating monthly statement for customer: {} - {}/{}", customerId, year, month);

        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

        Instant startDate = startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endDate = endOfMonth.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<CreditTransaction> transactions = transactionRepository.findByCustomerAndDateRange(
                customerId, startDate, endDate
        );

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map to full response
     */
    private CreditTransactionResponse mapToResponse(CreditTransaction t) {
        CreditTransactionResponse response = new CreditTransactionResponse();
        response.setId(t.getId());
        response.setTransactionNumber(t.getTransactionNumber());
        response.setCustomerId(t.getCustomer().getId());
        response.setCustomerCode(t.getCustomer().getCode());
        response.setCustomerName(t.getCustomer().getDisplayName());
        response.setTransactionType(t.getTransactionType().name());
        response.setStatus(t.getStatus().name());
        response.setTotalAmount(t.getTotalAmount());
        response.setTotalItems(t.getTotalItems());
        response.setPerformedBy(t.getPerformedBy());
        response.setPerformedByRole(t.getPerformedByRole().name());
        response.setConfirmedBy(t.getConfirmedBy());
        response.setConfirmedAt(t.getConfirmedAt());
        response.setSignatureData(t.getSignatureData());
        response.setPhotoData(t.getPhotoData());
        response.setNotes(t.getNotes());
        response.setCreatedAt(t.getCreatedAt());
        response.setUpdatedAt(t.getUpdatedAt());

        // Map lines
        List<CreditTransactionLineResponse> lineResponses = t.getLines().stream()
                .map(this::mapLineToResponse)
                .collect(Collectors.toList());
        response.setLines(lineResponses);

        return response;
    }

    /**
     * Map to summary response
     */
    private CreditTransactionSummaryResponse mapToSummaryResponse(CreditTransaction t) {
        return new CreditTransactionSummaryResponse(
                t.getId(),
                t.getTransactionNumber(),
                t.getCustomer().getCode(),
                t.getCustomer().getDisplayName(),
                t.getTransactionType().name(),
                t.getStatus().name(),
                t.getTotalAmount(),
                t.getTotalItems(),
                t.getPerformedBy(),
                t.getCreatedAt()
        );
    }

    /**
     * Map line to response
     */
    private CreditTransactionLineResponse mapLineToResponse(CreditTransactionLine line) {
        return new CreditTransactionLineResponse(
                line.getId(),
                line.getProduct().getId(),
                line.getProductCode(),
                line.getProductName(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getLineTotal(),
                line.getNotes()
        );
    }
}
