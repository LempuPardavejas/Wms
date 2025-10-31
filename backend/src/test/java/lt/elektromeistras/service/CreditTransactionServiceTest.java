package lt.elektromeistras.service;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.dto.request.*;
import lt.elektromeistras.dto.response.*;
import lt.elektromeistras.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreditTransactionService
 */
@ExtendWith(MockitoExtension.class)
class CreditTransactionServiceTest {

    @Mock
    private CreditTransactionRepository transactionRepository;

    @Mock
    private CreditTransactionLineRepository lineRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CreditTransactionService creditTransactionService;

    private Customer testCustomer;
    private Product testProduct1;
    private Product testProduct2;
    private CreditTransaction testTransaction;

    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setCode("B001");
        testCustomer.setCompanyName("UAB Test Company");
        testCustomer.setCustomerType(Customer.CustomerType.BUSINESS);
        testCustomer.setCreditLimit(new BigDecimal("5000.00"));
        testCustomer.setCurrentBalance(new BigDecimal("1000.00"));

        // Setup test products
        testProduct1 = new Product();
        testProduct1.setId(UUID.randomUUID());
        testProduct1.setCode("CAB-001");
        testProduct1.setName("Kabelis NYM 3x1.5");
        testProduct1.setBasePrice(new BigDecimal("2.50"));

        testProduct2 = new Product();
        testProduct2.setId(UUID.randomUUID());
        testProduct2.setCode("SW-001");
        testProduct2.setName("Jungiklis");
        testProduct2.setBasePrice(new BigDecimal("25.00"));

        // Setup test transaction
        testTransaction = new CreditTransaction();
        testTransaction.setId(UUID.randomUUID());
        testTransaction.setTransactionNumber("P1730000001");
        testTransaction.setCustomer(testCustomer);
        testTransaction.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        testTransaction.setStatus(CreditTransaction.TransactionStatus.PENDING);
        testTransaction.setPerformedBy("Jonas Jonaitis");
        testTransaction.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        testTransaction.setCreatedAt(Instant.now());
        testTransaction.setUpdatedAt(Instant.now());
    }

    @Test
    void createQuickCreditPickup_Success() {
        // Given
        QuickCreditPickupRequest request = new QuickCreditPickupRequest();
        request.setCustomerCode("B001");
        request.setPerformedBy("Jonas Jonaitis");
        request.setPerformedByRole("EMPLOYEE");

        List<QuickCreditPickupRequest.QuickCreditLineItem> items = new ArrayList<>();
        items.add(new QuickCreditPickupRequest.QuickCreditLineItem("CAB-001", new BigDecimal("50"), null));
        items.add(new QuickCreditPickupRequest.QuickCreditLineItem("SW-001", new BigDecimal("5"), null));
        request.setItems(items);

        when(customerRepository.findByCode("B001")).thenReturn(Optional.of(testCustomer));
        when(productRepository.findByCode("CAB-001")).thenReturn(Optional.of(testProduct1));
        when(productRepository.findByCode("SW-001")).thenReturn(Optional.of(testProduct2));
        when(transactionRepository.save(any(CreditTransaction.class))).thenReturn(testTransaction);

        // When
        CreditTransactionResponse response = creditTransactionService.createQuickCreditPickup(request);

        // Then
        assertNotNull(response);
        assertEquals("P1730000001", response.getTransactionNumber());
        assertEquals("B001", response.getCustomerCode());
        verify(transactionRepository, times(1)).save(any(CreditTransaction.class));
    }

    @Test
    void createQuickCreditPickup_CustomerNotFound() {
        // Given
        QuickCreditPickupRequest request = new QuickCreditPickupRequest();
        request.setCustomerCode("INVALID");

        when(customerRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            creditTransactionService.createQuickCreditPickup(request);
        });
    }

    @Test
    void confirmTransaction_Success() {
        // Given
        UUID transactionId = testTransaction.getId();
        ConfirmCreditTransactionRequest request = new ConfirmCreditTransactionRequest();
        request.setConfirmedBy("Petras Petraitis");

        testTransaction.setTotalAmount(new BigDecimal("250.00"));

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(CreditTransaction.class))).thenReturn(testTransaction);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CreditTransactionResponse response = creditTransactionService.confirmTransaction(transactionId, request);

        // Then
        assertNotNull(response);
        assertEquals(CreditTransaction.TransactionStatus.CONFIRMED.name(), response.getStatus());
        assertEquals("Petras Petraitis", response.getConfirmedBy());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void confirmTransaction_AlreadyConfirmed() {
        // Given
        UUID transactionId = testTransaction.getId();
        testTransaction.setStatus(CreditTransaction.TransactionStatus.CONFIRMED);
        ConfirmCreditTransactionRequest request = new ConfirmCreditTransactionRequest();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            creditTransactionService.confirmTransaction(transactionId, request);
        });
    }

    @Test
    void cancelTransaction_Success() {
        // Given
        UUID transactionId = testTransaction.getId();
        String reason = "Customer request";

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(CreditTransaction.class))).thenReturn(testTransaction);

        // When
        creditTransactionService.cancelTransaction(transactionId, reason);

        // Then
        verify(transactionRepository, times(1)).save(any(CreditTransaction.class));
    }

    @Test
    void cancelTransaction_Invoiced_ShouldFail() {
        // Given
        UUID transactionId = testTransaction.getId();
        testTransaction.setStatus(CreditTransaction.TransactionStatus.INVOICED);
        String reason = "Test";

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            creditTransactionService.cancelTransaction(transactionId, reason);
        });
    }

    @Test
    void getCustomerTransactions_Success() {
        // Given
        UUID customerId = testCustomer.getId();
        Pageable pageable = PageRequest.of(0, 20);
        List<CreditTransaction> transactions = Arrays.asList(testTransaction);
        Page<CreditTransaction> page = new PageImpl<>(transactions);

        when(transactionRepository.findByCustomerId(customerId, pageable)).thenReturn(page);

        // When
        Page<CreditTransactionSummaryResponse> result = creditTransactionService.getCustomerTransactions(customerId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(transactionRepository, times(1)).findByCustomerId(customerId, pageable);
    }

    @Test
    void getMonthlyStatement_Success() {
        // Given
        UUID customerId = testCustomer.getId();
        int year = 2025;
        int month = 10;

        List<CreditTransaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByCustomerAndDateRange(
                eq(customerId), any(Instant.class), any(Instant.class)))
                .thenReturn(transactions);

        // When
        List<CreditTransactionResponse> result = creditTransactionService.getMonthlyStatement(customerId, year, month);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findByCustomerAndDateRange(
                eq(customerId), any(Instant.class), any(Instant.class));
    }

    @Test
    void getTransactionById_Success() {
        // Given
        UUID transactionId = testTransaction.getId();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // When
        CreditTransactionResponse response = creditTransactionService.getTransactionById(transactionId);

        // Then
        assertNotNull(response);
        assertEquals(transactionId, response.getId());
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    void getTransactionById_NotFound() {
        // Given
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            creditTransactionService.getTransactionById(transactionId);
        });
    }

    @Test
    void searchTransactions_Success() {
        // Given
        String query = "B001";
        Pageable pageable = PageRequest.of(0, 20);
        List<CreditTransaction> transactions = Arrays.asList(testTransaction);
        Page<CreditTransaction> page = new PageImpl<>(transactions);

        when(transactionRepository.searchTransactions(query, pageable)).thenReturn(page);

        // When
        Page<CreditTransactionSummaryResponse> result = creditTransactionService.searchTransactions(query, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(transactionRepository, times(1)).searchTransactions(query, pageable);
    }
}
