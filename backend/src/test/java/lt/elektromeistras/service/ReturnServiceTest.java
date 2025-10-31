package lt.elektromeistras.service;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.dto.request.*;
import lt.elektromeistras.dto.response.ReturnResponse;
import lt.elektromeistras.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReturnService
 * Tests cover the complete returns workflow:
 * - Return creation
 * - Return approval/rejection
 * - Return receiving
 * - Return inspection
 * - Restocking
 * - Refund processing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnService Unit Tests")
class ReturnServiceTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ReturnLineRepository returnLineRepository;

    @Mock
    private ReturnReasonRepository returnReasonRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseLocationRepository warehouseLocationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private ReturnService returnService;

    private Order testOrder;
    private Customer testCustomer;
    private Warehouse testWarehouse;
    private Product testProduct;
    private OrderLine testOrderLine;
    private ReturnReason testReturnReason;
    private Return testReturn;

    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setCode("CUST001");
        testCustomer.setFirstName("Test");
        testCustomer.setLastName("Customer");

        // Setup test warehouse
        testWarehouse = new Warehouse();
        testWarehouse.setId(UUID.randomUUID());
        testWarehouse.setCode("WH001");
        testWarehouse.setName("Main Warehouse");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setSku("PROD001");
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.00));

        // Setup test order
        testOrder = new Order();
        testOrder.setId(UUID.randomUUID());
        testOrder.setOrderNumber("ORD-20250101-0001");
        testOrder.setCustomer(testCustomer);
        testOrder.setStatus(Order.OrderStatus.COMPLETED);

        // Setup test order line
        testOrderLine = new OrderLine();
        testOrderLine.setId(UUID.randomUUID());
        testOrderLine.setOrder(testOrder);
        testOrderLine.setProduct(testProduct);
        testOrderLine.setQuantity(BigDecimal.valueOf(5));
        testOrderLine.setUnitPrice(BigDecimal.valueOf(100.00));
        testOrderLine.setTaxRate(BigDecimal.valueOf(21.00));

        // Setup test return reason
        testReturnReason = new ReturnReason();
        testReturnReason.setId(UUID.randomUUID());
        testReturnReason.setCode("DEFECTIVE");
        testReturnReason.setName("Product Defective");
        testReturnReason.setAllowsRestock(true);
        testReturnReason.setRequiresInspection(true);

        // Setup test return
        testReturn = new Return();
        testReturn.setId(UUID.randomUUID());
        testReturn.setReturnNumber("RET-20250101-0001");
        testReturn.setOrder(testOrder);
        testReturn.setCustomer(testCustomer);
        testReturn.setWarehouse(testWarehouse);
        testReturn.setStatus(Return.ReturnStatus.PENDING);
        testReturn.setRefundStatus(Return.RefundStatus.PENDING);
    }

    @Test
    @DisplayName("Should create return successfully")
    void shouldCreateReturn() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrder.getId());
        request.setCustomerId(testCustomer.getId());
        request.setWarehouseId(testWarehouse.getId());
        request.setNotes("Customer wants to return defective product");

        ReturnLineRequest lineRequest = new ReturnLineRequest();
        lineRequest.setOrderLineId(testOrderLine.getId());
        lineRequest.setProductId(testProduct.getId());
        lineRequest.setReturnReasonId(testReturnReason.getId());
        lineRequest.setQuantityReturned(BigDecimal.valueOf(2));
        lineRequest.setNotes("Product not working");

        request.setLines(List.of(lineRequest));

        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        when(customerRepository.findById(testCustomer.getId())).thenReturn(Optional.of(testCustomer));
        when(warehouseRepository.findById(testWarehouse.getId())).thenReturn(Optional.of(testWarehouse));
        when(orderLineRepository.findById(testOrderLine.getId())).thenReturn(Optional.of(testOrderLine));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(returnReasonRepository.findById(testReturnReason.getId())).thenReturn(Optional.of(testReturnReason));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> {
            Return savedReturn = invocation.getArgument(0);
            savedReturn.setId(UUID.randomUUID());
            return savedReturn;
        });

        // When
        ReturnResponse result = returnService.createReturn(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReturnNumber()).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(testOrder.getId());
        assertThat(result.getCustomerId()).isEqualTo(testCustomer.getId());
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(returnRepository).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when creating return for non-completed order")
    void shouldThrowExceptionForNonCompletedOrder() {
        // Given
        testOrder.setStatus(Order.OrderStatus.DRAFT);
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrder.getId());
        request.setCustomerId(testCustomer.getId());
        request.setWarehouseId(testWarehouse.getId());

        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        when(customerRepository.findById(testCustomer.getId())).thenReturn(Optional.of(testCustomer));
        when(warehouseRepository.findById(testWarehouse.getId())).thenReturn(Optional.of(testWarehouse));

        // When & Then
        assertThatThrownBy(() -> returnService.createReturn(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only completed orders can be returned");
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when return quantity exceeds ordered quantity")
    void shouldThrowExceptionWhenReturnQuantityExceedsOrdered() {
        // Given
        CreateReturnRequest request = new CreateReturnRequest();
        request.setOrderId(testOrder.getId());
        request.setCustomerId(testCustomer.getId());
        request.setWarehouseId(testWarehouse.getId());

        ReturnLineRequest lineRequest = new ReturnLineRequest();
        lineRequest.setOrderLineId(testOrderLine.getId());
        lineRequest.setProductId(testProduct.getId());
        lineRequest.setReturnReasonId(testReturnReason.getId());
        lineRequest.setQuantityReturned(BigDecimal.valueOf(10)); // More than ordered (5)

        request.setLines(List.of(lineRequest));

        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        when(customerRepository.findById(testCustomer.getId())).thenReturn(Optional.of(testCustomer));
        when(warehouseRepository.findById(testWarehouse.getId())).thenReturn(Optional.of(testWarehouse));
        when(orderLineRepository.findById(testOrderLine.getId())).thenReturn(Optional.of(testOrderLine));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(returnReasonRepository.findById(testReturnReason.getId())).thenReturn(Optional.of(testReturnReason));

        // When & Then
        assertThatThrownBy(() -> returnService.createReturn(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot return more than ordered quantity");
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should approve return successfully")
    void shouldApproveReturn() {
        // Given
        UUID returnId = testReturn.getId();
        String notes = "Approved by manager";
        testReturn.setStatus(Return.ReturnStatus.PENDING);

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnResponse result = returnService.approveReturn(returnId, notes);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(testReturn.getExpectedDate()).isNotNull();
        verify(returnRepository).findById(returnId);
        verify(returnRepository).save(testReturn);
    }

    @Test
    @DisplayName("Should throw exception when approving non-pending return")
    void shouldThrowExceptionWhenApprovingNonPendingReturn() {
        // Given
        UUID returnId = testReturn.getId();
        testReturn.setStatus(Return.ReturnStatus.APPROVED);

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));

        // When & Then
        assertThatThrownBy(() -> returnService.approveReturn(returnId, "notes"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending returns can be approved");
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should reject return successfully")
    void shouldRejectReturn() {
        // Given
        UUID returnId = testReturn.getId();
        String rejectionReason = "Outside return window";
        testReturn.setStatus(Return.ReturnStatus.PENDING);

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnResponse result = returnService.rejectReturn(returnId, rejectionReason);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("REJECTED");
        assertThat(testReturn.getRejectionReason()).isEqualTo(rejectionReason);
        verify(returnRepository).findById(returnId);
        verify(returnRepository).save(testReturn);
    }

    @Test
    @DisplayName("Should mark return as received successfully")
    void shouldMarkAsReceived() {
        // Given
        UUID returnId = testReturn.getId();
        testReturn.setStatus(Return.ReturnStatus.APPROVED);

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnResponse result = returnService.markAsReceived(returnId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("RECEIVED");
        assertThat(testReturn.getReceivedDate()).isNotNull();
        verify(returnRepository).findById(returnId);
        verify(returnRepository).save(testReturn);
    }

    @Test
    @DisplayName("Should inspect return successfully")
    void shouldInspectReturn() {
        // Given
        UUID returnId = testReturn.getId();
        testReturn.setStatus(Return.ReturnStatus.RECEIVED);

        ReturnLine returnLine = new ReturnLine();
        returnLine.setId(UUID.randomUUID());
        returnLine.setReturnEntity(testReturn);
        returnLine.setProduct(testProduct);
        returnLine.setReturnReason(testReturnReason);
        returnLine.setQuantityReturned(BigDecimal.valueOf(2));
        returnLine.setUnitPrice(BigDecimal.valueOf(100.00));
        returnLine.setTaxRate(BigDecimal.valueOf(21.00));
        testReturn.addLine(returnLine);

        InspectReturnLineRequest inspection = new InspectReturnLineRequest();
        inspection.setReturnLineId(returnLine.getId());
        inspection.setQuantityAccepted(BigDecimal.valueOf(2));
        inspection.setQuantityRejected(BigDecimal.ZERO);
        inspection.setCondition("GOOD");
        inspection.setInspectionNotes("Product in good condition");

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));
        when(returnLineRepository.findById(returnLine.getId())).thenReturn(Optional.of(returnLine));
        when(returnLineRepository.save(any(ReturnLine.class))).thenReturn(returnLine);
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnResponse result = returnService.inspectReturn(returnId, List.of(inspection));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("INSPECTED");
        assertThat(testReturn.getInspectedDate()).isNotNull();
        assertThat(returnLine.getCondition()).isEqualTo(ReturnLine.ProductCondition.GOOD);
        assertThat(returnLine.getQuantityAccepted()).isEqualByComparingTo(BigDecimal.valueOf(2));
        verify(returnRepository).findById(returnId);
        verify(returnRepository).save(testReturn);
    }

    @Test
    @DisplayName("Should throw exception when inspected quantities don't match returned")
    void shouldThrowExceptionWhenInspectedQuantitiesMismatch() {
        // Given
        UUID returnId = testReturn.getId();
        testReturn.setStatus(Return.ReturnStatus.RECEIVED);

        ReturnLine returnLine = new ReturnLine();
        returnLine.setId(UUID.randomUUID());
        returnLine.setQuantityReturned(BigDecimal.valueOf(2));
        testReturn.addLine(returnLine);

        InspectReturnLineRequest inspection = new InspectReturnLineRequest();
        inspection.setReturnLineId(returnLine.getId());
        inspection.setQuantityAccepted(BigDecimal.valueOf(1));
        inspection.setQuantityRejected(BigDecimal.ZERO); // Total = 1, but returned = 2

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));
        when(returnLineRepository.findById(returnLine.getId())).thenReturn(Optional.of(returnLine));

        // When & Then
        assertThatThrownBy(() -> returnService.inspectReturn(returnId, List.of(inspection)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Accepted + Rejected must equal returned quantity");
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should restock return successfully")
    void shouldRestockReturn() {
        // Given
        UUID returnId = testReturn.getId();
        testReturn.setStatus(Return.ReturnStatus.INSPECTED);

        ReturnLine returnLine = new ReturnLine();
        returnLine.setId(UUID.randomUUID());
        returnLine.setReturnEntity(testReturn);
        returnLine.setProduct(testProduct);
        returnLine.setReturnReason(testReturnReason);
        returnLine.setQuantityAccepted(BigDecimal.valueOf(2));
        returnLine.setRestockEligible(true);
        returnLine.setRestocked(false);
        returnLine.setCondition(ReturnLine.ProductCondition.GOOD);
        testReturn.addLine(returnLine);

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));
        when(returnLineRepository.save(any(ReturnLine.class))).thenReturn(returnLine);
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);
        doNothing().when(stockService).restockFromReturn(any(ReturnLine.class));

        // When
        ReturnResponse result = returnService.restockReturn(returnId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(returnLine.getRestocked()).isTrue();
        assertThat(returnLine.getRestockedDate()).isNotNull();
        verify(stockService).restockFromReturn(returnLine);
        verify(returnRepository).save(testReturn);
    }

    @Test
    @DisplayName("Should process refund successfully")
    void shouldProcessRefund() {
        // Given
        UUID returnId = testReturn.getId();
        testReturn.setStatus(Return.ReturnStatus.COMPLETED);
        testReturn.setRefundStatus(Return.RefundStatus.PENDING);
        testReturn.setRefundAmount(BigDecimal.valueOf(200.00));

        ProcessRefundRequest request = new ProcessRefundRequest();
        request.setRefundAmount(BigDecimal.valueOf(200.00));
        request.setRefundMethod("BANK_TRANSFER");
        request.setRefundReference("REF123456");
        request.setNotes("Refund processed successfully");

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnResponse result = returnService.processRefund(returnId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRefundStatus()).isEqualTo("COMPLETED");
        assertThat(testReturn.getRefundDate()).isNotNull();
        assertThat(testReturn.getRefundMethod()).isEqualTo("BANK_TRANSFER");
        assertThat(testReturn.getRefundReference()).isEqualTo("REF123456");
        verify(returnRepository).findById(returnId);
        verify(returnRepository).save(testReturn);
    }

    @Test
    @DisplayName("Should throw exception when processing refund for non-completed return")
    void shouldThrowExceptionWhenProcessingRefundForNonCompletedReturn() {
        // Given
        UUID returnId = testReturn.getId();
        testReturn.setStatus(Return.ReturnStatus.INSPECTED);

        ProcessRefundRequest request = new ProcessRefundRequest();
        request.setRefundAmount(BigDecimal.valueOf(200.00));

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));

        // When & Then
        assertThatThrownBy(() -> returnService.processRefund(returnId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Return must be completed before processing refund");
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when refund amount exceeds calculated amount")
    void shouldThrowExceptionWhenRefundAmountExceedsCalculated() {
        // Given
        UUID returnId = testReturn.getId();
        testReturn.setStatus(Return.ReturnStatus.COMPLETED);
        testReturn.setRefundStatus(Return.RefundStatus.PENDING);
        testReturn.setRefundAmount(BigDecimal.valueOf(100.00));

        ProcessRefundRequest request = new ProcessRefundRequest();
        request.setRefundAmount(BigDecimal.valueOf(200.00)); // Exceeds calculated amount

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));

        // When & Then
        assertThatThrownBy(() -> returnService.processRefund(returnId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refund amount cannot exceed calculated refund amount");
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should get return by ID successfully")
    void shouldGetReturnById() {
        // Given
        UUID returnId = testReturn.getId();
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(testReturn));

        // When
        ReturnResponse result = returnService.getReturn(returnId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(returnId);
        verify(returnRepository).findById(returnId);
    }

    @Test
    @DisplayName("Should throw exception when return not found by ID")
    void shouldThrowExceptionWhenReturnNotFoundById() {
        // Given
        UUID returnId = UUID.randomUUID();
        when(returnRepository.findById(returnId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> returnService.getReturn(returnId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Return not found");
        verify(returnRepository).findById(returnId);
    }
}
