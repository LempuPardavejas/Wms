package lt.elektromeistras.service;

import lt.elektromeistras.domain.Customer;
import lt.elektromeistras.domain.Order;
import lt.elektromeistras.domain.OrderLine;
import lt.elektromeistras.domain.Product;
import lt.elektromeistras.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService
 * Tests cover:
 * - Order creation and validation
 * - Quick order entry
 * - Order number generation
 * - Order status transitions
 * - Order retrieval and search
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setCode("CUST001");
        testCustomer.setName("Test Customer");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setSku("PROD001");
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
        testProduct.setTaxRate(BigDecimal.valueOf(21.00));

        // Setup test order
        testOrder = new Order();
        testOrder.setId(UUID.randomUUID());
        testOrder.setOrderNumber("ORD-20250101-0001");
        testOrder.setCustomer(testCustomer);
        testOrder.setStatus(Order.OrderStatus.DRAFT);
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void shouldGetOrderById() {
        // Given
        UUID orderId = testOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.getById(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getOrderNumber()).isEqualTo("ORD-20250101-0001");
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void shouldThrowExceptionWhenOrderNotFoundById() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getById(orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found with id");
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should get order by order number successfully")
    void shouldGetOrderByOrderNumber() {
        // Given
        String orderNumber = "ORD-20250101-0001";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.getByOrderNumber(orderNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    @DisplayName("Should get orders by customer with pagination")
    void shouldGetOrdersByCustomer() {
        // Given
        UUID customerId = testCustomer.getId();
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findByCustomerId(customerId, pageable)).thenReturn(orderPage);

        // When
        Page<Order> result = orderService.getOrdersByCustomer(customerId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCustomer().getId()).isEqualTo(customerId);
        verify(orderRepository).findByCustomerId(customerId, pageable);
    }

    @Test
    @DisplayName("Should get completed orders by customer")
    void shouldGetCompletedOrdersByCustomer() {
        // Given
        UUID customerId = testCustomer.getId();
        testOrder.setStatus(Order.OrderStatus.COMPLETED);
        List<Order> completedOrders = List.of(testOrder);

        when(orderRepository.findCompletedOrdersByCustomerId(customerId)).thenReturn(completedOrders);

        // When
        List<Order> result = orderService.getCompletedOrdersByCustomer(customerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
        verify(orderRepository).findCompletedOrdersByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should create order with lines successfully")
    void shouldCreateOrderWithLines() {
        // Given
        Order newOrder = new Order();
        newOrder.setCustomer(testCustomer);

        OrderLine orderLine = new OrderLine();
        orderLine.setProduct(testProduct);
        orderLine.setQuantity(BigDecimal.valueOf(2));
        List<OrderLine> orderLines = List.of(orderLine);

        when(customerService.getById(testCustomer.getId())).thenReturn(testCustomer);
        when(productService.getById(testProduct.getId())).thenReturn(testProduct);
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());
        when(orderRepository.countOrdersSince(any(Instant.class))).thenReturn(0L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(UUID.randomUUID());
            return savedOrder;
        });

        // When
        Order result = orderService.createOrder(newOrder, orderLines);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isNotNull();
        assertThat(result.getCustomer()).isEqualTo(testCustomer);
        assertThat(result.getOrderLines()).hasSize(1);
        verify(customerService).getById(testCustomer.getId());
        verify(productService).getById(testProduct.getId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when creating order with duplicate order number")
    void shouldThrowExceptionForDuplicateOrderNumber() {
        // Given
        Order newOrder = new Order();
        newOrder.setCustomer(testCustomer);
        newOrder.setOrderNumber("ORD-20250101-0001");

        when(customerService.getById(testCustomer.getId())).thenReturn(testCustomer);
        when(orderRepository.findByOrderNumber("ORD-20250101-0001")).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(newOrder, new ArrayList<>()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order with number already exists");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create quick order successfully")
    void shouldCreateQuickOrder() {
        // Given
        UUID customerId = testCustomer.getId();
        List<OrderService.QuickOrderLine> quickLines = List.of(
                new OrderService.QuickOrderLine("PROD001", BigDecimal.valueOf(5)),
                new OrderService.QuickOrderLine("PROD002", BigDecimal.valueOf(3))
        );

        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setSku("PROD002");
        product2.setName("Test Product 2");
        product2.setPrice(BigDecimal.valueOf(50.00));
        product2.setTaxRate(BigDecimal.valueOf(21.00));

        when(customerService.getById(customerId)).thenReturn(testCustomer);
        when(productService.getByCode("PROD001")).thenReturn(testProduct);
        when(productService.getByCode("PROD002")).thenReturn(product2);
        when(orderRepository.countOrdersSince(any(Instant.class))).thenReturn(0L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(UUID.randomUUID());
            return savedOrder;
        });

        // When
        Order result = orderService.createQuickOrder(customerId, quickLines);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isNotNull();
        assertThat(result.getCustomer()).isEqualTo(testCustomer);
        assertThat(result.getOrderLines()).hasSize(2);
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.DRAFT);
        verify(customerService).getById(customerId);
        verify(productService).getByCode("PROD001");
        verify(productService).getByCode("PROD002");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should confirm order successfully")
    void shouldConfirmOrder() {
        // Given
        UUID orderId = testOrder.getId();
        testOrder.setStatus(Order.OrderStatus.DRAFT);

        OrderLine orderLine = new OrderLine();
        orderLine.setProduct(testProduct);
        orderLine.setQuantity(BigDecimal.valueOf(1));
        testOrder.addOrderLine(orderLine);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.confirmOrder(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when confirming non-draft order")
    void shouldThrowExceptionWhenConfirmingNonDraftOrder() {
        // Given
        UUID orderId = testOrder.getId();
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.confirmOrder(orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only DRAFT orders can be confirmed");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when confirming order with no lines")
    void shouldThrowExceptionWhenConfirmingOrderWithNoLines() {
        // Given
        UUID orderId = testOrder.getId();
        testOrder.setStatus(Order.OrderStatus.DRAFT);
        testOrder.getOrderLines().clear();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.confirmOrder(orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot confirm order with no line items");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrder() {
        // Given
        UUID orderId = testOrder.getId();
        String cancelReason = "Customer requested cancellation";
        testOrder.setStatus(Order.OrderStatus.DRAFT);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.cancelOrder(orderId, cancelReason);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
        assertThat(result.getNotes()).contains("Cancelled: " + cancelReason);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when cancelling completed order")
    void shouldThrowExceptionWhenCancellingCompletedOrder() {
        // Given
        UUID orderId = testOrder.getId();
        testOrder.setStatus(Order.OrderStatus.COMPLETED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, "test reason"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot cancel completed or shipped orders");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling shipped order")
    void shouldThrowExceptionWhenCancellingShippedOrder() {
        // Given
        UUID orderId = testOrder.getId();
        testOrder.setStatus(Order.OrderStatus.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, "test reason"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot cancel completed or shipped orders");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should search orders successfully")
    void shouldSearchOrders() {
        // Given
        String query = "ORD-20250101";
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.searchOrders(query, pageable)).thenReturn(orderPage);

        // When
        Page<Order> result = orderService.searchOrders(query, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).searchOrders(query, pageable);
    }

    @Test
    @DisplayName("Should return all orders when search query is empty")
    void shouldReturnAllOrdersWhenSearchQueryIsEmpty() {
        // Given
        String query = "   ";
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // When
        Page<Order> result = orderService.searchOrders(query, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAll(pageable);
        verify(orderRepository, never()).searchOrders(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get orders by status")
    void shouldGetOrdersByStatus() {
        // Given
        Order.OrderStatus status = Order.OrderStatus.CONFIRMED;
        Pageable pageable = PageRequest.of(0, 10);
        testOrder.setStatus(status);
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findByStatus(status, pageable)).thenReturn(orderPage);

        // When
        Page<Order> result = orderService.getOrdersByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(status);
        verify(orderRepository).findByStatus(status, pageable);
    }

    @Test
    @DisplayName("Should update order successfully")
    void shouldUpdateOrder() {
        // Given
        UUID orderId = testOrder.getId();
        Order orderDetails = new Order();
        orderDetails.setStatus(Order.OrderStatus.CONFIRMED);
        orderDetails.setNotes("Updated notes");
        orderDetails.setDeliveryAddress("New Address");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.updateOrder(orderId, orderDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        assertThat(result.getNotes()).isEqualTo("Updated notes");
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(testOrder);
    }
}
