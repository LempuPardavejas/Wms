package lt.elektromeistras.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.elektromeistras.domain.Customer;
import lt.elektromeistras.domain.Order;
import lt.elektromeistras.domain.OrderLine;
import lt.elektromeistras.domain.Product;
import lt.elektromeistras.repository.CustomerRepository;
import lt.elektromeistras.repository.OrderRepository;
import lt.elektromeistras.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Order REST API
 * Tests the complete request/response cycle including:
 * - Authentication
 * - Request handling
 * - Service layer
 * - Database operations
 * - Response formatting
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Order API Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    private Customer testCustomer;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Clean up
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();

        // Create test data
        testCustomer = new Customer();
        testCustomer.setCode("TEST-CUST-001");
        testCustomer.setName("Integration Test Customer");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPhone("+370 600 12345");
        testCustomer.setType(Customer.CustomerType.RETAIL);
        testCustomer = customerRepository.save(testCustomer);

        testProduct = new Product();
        testProduct.setSku("TEST-PROD-001");
        testProduct.setName("Integration Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
        testProduct.setTaxRate(BigDecimal.valueOf(21.00));
        testProduct = productRepository.save(testProduct);

        testOrder = new Order();
        testOrder.setOrderNumber("TEST-ORD-001");
        testOrder.setCustomer(testCustomer);
        testOrder.setStatus(Order.OrderStatus.DRAFT);

        OrderLine orderLine = new OrderLine();
        orderLine.initializeFromProduct(testProduct);
        orderLine.setQuantity(BigDecimal.valueOf(2));
        orderLine.calculateAmounts();
        testOrder.addOrderLine(orderLine);
        testOrder.calculateTotals();

        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Should return order by ID")
    @WithMockUser(authorities = {"SALES_VIEW"})
    void shouldGetOrderById() throws Exception {
        mockMvc.perform(get("/api/orders/" + testOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrder.getId().toString()))
                .andExpect(jsonPath("$.orderNumber").value("TEST-ORD-001"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.customer.id").value(testCustomer.getId().toString()))
                .andExpect(jsonPath("$.orderLines").isArray())
                .andExpect(jsonPath("$.orderLines", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Should return 401 without authentication")
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders/" + testOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Should return 404 for non-existent order")
    @WithMockUser(authorities = {"SALES_VIEW"})
    void shouldReturn404ForNonExistentOrder() throws Exception {
        mockMvc.perform(get("/api/orders/" + java.util.UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/orders/number/{orderNumber} - Should return order by order number")
    @WithMockUser(authorities = {"SALES_VIEW"})
    void shouldGetOrderByOrderNumber() throws Exception {
        mockMvc.perform(get("/api/orders/number/TEST-ORD-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("TEST-ORD-001"))
                .andExpect(jsonPath("$.id").value(testOrder.getId().toString()));
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - Should return orders by customer")
    @WithMockUser(authorities = {"SALES_VIEW"})
    void shouldGetOrdersByCustomer() throws Exception {
        mockMvc.perform(get("/api/orders/customer/" + testCustomer.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].customer.id").value(testCustomer.getId().toString()));
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId}/completed - Should return completed orders")
    @WithMockUser(authorities = {"SALES_VIEW"})
    void shouldGetCompletedOrdersByCustomer() throws Exception {
        // First, create a completed order
        Order completedOrder = new Order();
        completedOrder.setOrderNumber("TEST-ORD-COMPLETED");
        completedOrder.setCustomer(testCustomer);
        completedOrder.setStatus(Order.OrderStatus.COMPLETED);

        OrderLine orderLine = new OrderLine();
        orderLine.initializeFromProduct(testProduct);
        orderLine.setQuantity(BigDecimal.ONE);
        orderLine.calculateAmounts();
        completedOrder.addOrderLine(orderLine);
        completedOrder.calculateTotals();

        orderRepository.save(completedOrder);

        mockMvc.perform(get("/api/orders/customer/" + testCustomer.getId() + "/completed")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].orderNumber").value("TEST-ORD-COMPLETED"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/orders - Should return all orders with pagination")
    @WithMockUser(authorities = {"SALES_VIEW"})
    void shouldGetAllOrdersWithPagination() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @DisplayName("GET /api/orders/search - Should search orders")
    @WithMockUser(authorities = {"SALES_VIEW"})
    void shouldSearchOrders() throws Exception {
        mockMvc.perform(get("/api/orders/search")
                        .param("q", "TEST-ORD")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/orders/quick - Should create quick order")
    @WithMockUser(authorities = {"SALES_CREATE"})
    void shouldCreateQuickOrder() throws Exception {
        String quickOrderJson = """
                {
                    "customerId": "%s",
                    "lines": [
                        {
                            "productCode": "TEST-PROD-001",
                            "quantity": 3
                        }
                    ]
                }
                """.formatted(testCustomer.getId());

        mockMvc.perform(post("/api/orders/quick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(quickOrderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.customer.id").value(testCustomer.getId().toString()))
                .andExpect(jsonPath("$.orderLines", hasSize(1)))
                .andExpect(jsonPath("$.orderLines[0].quantity").value(3));
    }
}
