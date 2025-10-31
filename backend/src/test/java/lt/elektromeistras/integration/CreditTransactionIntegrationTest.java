package lt.elektromeistras.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.elektromeistras.domain.*;
import lt.elektromeistras.dto.request.*;
import lt.elektromeistras.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Credit Transaction API
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CreditTransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CreditTransactionRepository transactionRepository;

    private Customer testCustomer;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Clean up
        transactionRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer
        testCustomer = new Customer();
        testCustomer.setCode("TEST001");
        testCustomer.setCompanyName("Test Company Ltd");
        testCustomer.setCustomerType(Customer.CustomerType.BUSINESS);
        testCustomer.setCreditLimit(new BigDecimal("5000.00"));
        testCustomer.setCurrentBalance(new BigDecimal("1000.00"));
        testCustomer = customerRepository.save(testCustomer);

        // Create test products
        testProduct1 = new Product();
        testProduct1.setCode("PROD-001");
        testProduct1.setSku("SKU-001");
        testProduct1.setName("Test Product 1");
        testProduct1.setBasePrice(new BigDecimal("10.00"));
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setCode("PROD-002");
        testProduct2.setSku("SKU-002");
        testProduct2.setName("Test Product 2");
        testProduct2.setBasePrice(new BigDecimal("20.00"));
        testProduct2 = productRepository.save(testProduct2);
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_MANAGE"})
    void createQuickCreditPickup_Success() throws Exception {
        // Given
        QuickCreditPickupRequest request = new QuickCreditPickupRequest();
        request.setCustomerCode("TEST001");
        request.setPerformedBy("Test Employee");
        request.setPerformedByRole("EMPLOYEE");

        List<QuickCreditPickupRequest.QuickCreditLineItem> items = new ArrayList<>();
        items.add(new QuickCreditPickupRequest.QuickCreditLineItem("PROD-001", new BigDecimal("5"), "Test note"));
        items.add(new QuickCreditPickupRequest.QuickCreditLineItem("PROD-002", new BigDecimal("3"), null));
        request.setItems(items);

        // When & Then
        mockMvc.perform(post("/api/credit-transactions/quick-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionNumber", notNullValue()))
                .andExpect(jsonPath("$.customerCode", is("TEST001")))
                .andExpect(jsonPath("$.transactionType", is("PICKUP")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.lines", hasSize(2)))
                .andExpect(jsonPath("$.totalAmount", is(110.0))) // (5 * 10) + (3 * 20) = 110
                .andExpect(jsonPath("$.totalItems", is(8))); // 5 + 3 = 8
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_MANAGE"})
    void createQuickCreditPickup_CustomerNotFound() throws Exception {
        // Given
        QuickCreditPickupRequest request = new QuickCreditPickupRequest();
        request.setCustomerCode("INVALID");
        request.setPerformedBy("Test Employee");
        request.setPerformedByRole("EMPLOYEE");
        request.setItems(new ArrayList<>());

        // When & Then
        mockMvc.perform(post("/api/credit-transactions/quick-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_MANAGE"})
    void confirmTransaction_Success() throws Exception {
        // Given - Create a pending transaction first
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCustomer(testCustomer);
        transaction.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        transaction.setStatus(CreditTransaction.TransactionStatus.PENDING);
        transaction.setTotalAmount(new BigDecimal("100.00"));
        transaction.setTotalItems(5);
        transaction.setPerformedBy("Test Employee");
        transaction.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        transaction = transactionRepository.save(transaction);

        ConfirmCreditTransactionRequest request = new ConfirmCreditTransactionRequest();
        request.setConfirmedBy("Test Manager");

        // When & Then
        mockMvc.perform(post("/api/credit-transactions/" + transaction.getId() + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.confirmedBy", is("Test Manager")))
                .andExpect(jsonPath("$.confirmedAt", notNullValue()));
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_MANAGE"})
    void cancelTransaction_Success() throws Exception {
        // Given - Create a pending transaction first
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCustomer(testCustomer);
        transaction.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        transaction.setStatus(CreditTransaction.TransactionStatus.PENDING);
        transaction.setTotalAmount(new BigDecimal("100.00"));
        transaction.setTotalItems(5);
        transaction.setPerformedBy("Test Employee");
        transaction.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        transaction = transactionRepository.save(transaction);

        // When & Then
        mockMvc.perform(post("/api/credit-transactions/" + transaction.getId() + "/cancel")
                        .param("reason", "Test cancellation"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_VIEW"})
    void getTransactionById_Success() throws Exception {
        // Given - Create a transaction first
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCustomer(testCustomer);
        transaction.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        transaction.setStatus(CreditTransaction.TransactionStatus.PENDING);
        transaction.setTotalAmount(new BigDecimal("100.00"));
        transaction.setTotalItems(5);
        transaction.setPerformedBy("Test Employee");
        transaction.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        transaction = transactionRepository.save(transaction);

        // When & Then
        mockMvc.perform(get("/api/credit-transactions/" + transaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(transaction.getId().toString())))
                .andExpect(jsonPath("$.customerCode", is("TEST001")))
                .andExpect(jsonPath("$.totalAmount", is(100.0)));
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_VIEW"})
    void getCustomerTransactions_Success() throws Exception {
        // Given - Create transactions for customer
        CreditTransaction transaction1 = new CreditTransaction();
        transaction1.setCustomer(testCustomer);
        transaction1.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        transaction1.setStatus(CreditTransaction.TransactionStatus.CONFIRMED);
        transaction1.setTotalAmount(new BigDecimal("100.00"));
        transaction1.setTotalItems(5);
        transaction1.setPerformedBy("Test Employee");
        transaction1.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        transactionRepository.save(transaction1);

        CreditTransaction transaction2 = new CreditTransaction();
        transaction2.setCustomer(testCustomer);
        transaction2.setTransactionType(CreditTransaction.TransactionType.RETURN);
        transaction2.setStatus(CreditTransaction.TransactionStatus.CONFIRMED);
        transaction2.setTotalAmount(new BigDecimal("50.00"));
        transaction2.setTotalItems(2);
        transaction2.setPerformedBy("Test Employee");
        transaction2.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        transactionRepository.save(transaction2);

        // When & Then
        mockMvc.perform(get("/api/credit-transactions/customer/" + testCustomer.getId())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].customerCode", everyItem(is("TEST001"))));
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_VIEW"})
    void searchTransactions_Success() throws Exception {
        // Given - Create a transaction
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCustomer(testCustomer);
        transaction.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        transaction.setStatus(CreditTransaction.TransactionStatus.CONFIRMED);
        transaction.setTotalAmount(new BigDecimal("100.00"));
        transaction.setTotalItems(5);
        transaction.setPerformedBy("Test Employee");
        transaction.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        transactionRepository.save(transaction);

        // When & Then
        mockMvc.perform(get("/api/credit-transactions/search")
                        .param("q", "TEST001")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_VIEW"})
    void getAllTransactions_Success() throws Exception {
        // Given - Create a transaction
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCustomer(testCustomer);
        transaction.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        transaction.setStatus(CreditTransaction.TransactionStatus.PENDING);
        transaction.setTotalAmount(new BigDecimal("100.00"));
        transaction.setTotalItems(5);
        transaction.setPerformedBy("Test Employee");
        transaction.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        transactionRepository.save(transaction);

        // When & Then
        mockMvc.perform(get("/api/credit-transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(authorities = {"CREDIT_VIEW"})
    void getMonthlyStatement_Success() throws Exception {
        // Given - Create confirmed transactions
        CreditTransaction transaction = new CreditTransaction();
        transaction.setCustomer(testCustomer);
        transaction.setTransactionType(CreditTransaction.TransactionType.PICKUP);
        transaction.setStatus(CreditTransaction.TransactionStatus.CONFIRMED);
        transaction.setTotalAmount(new BigDecimal("100.00"));
        transaction.setTotalItems(5);
        transaction.setPerformedBy("Test Employee");
        transaction.setPerformedByRole(CreditTransaction.PerformedByRole.EMPLOYEE);
        transactionRepository.save(transaction);

        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        // When & Then
        mockMvc.perform(get("/api/credit-transactions/customer/" + testCustomer.getId() +
                        "/statement/" + currentYear + "/" + currentMonth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
