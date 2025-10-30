package lt.elektromeistras.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.elektromeistras.domain.*;
import lt.elektromeistras.dto.request.*;
import lt.elektromeistras.dto.response.*;
import lt.elektromeistras.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Return Management Service
 * Handles the complete customer returns workflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final ReturnLineRepository returnLineRepository;
    private final ReturnReasonRepository returnReasonRepository;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationRepository warehouseLocationRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    @Transactional
    public ReturnResponse createReturn(CreateReturnRequest request) {
        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.getOrderId()));

        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerId()));

        // Validate warehouse
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + request.getWarehouseId()));

        // Validate order is completed
        if (order.getStatus() != Order.OrderStatus.COMPLETED) {
            throw new IllegalStateException("Only completed orders can be returned");
        }

        // Create return entity
        Return returnEntity = Return.builder()
                .returnNumber(generateReturnNumber())
                .order(order)
                .customer(customer)
                .warehouse(warehouse)
                .status(Return.ReturnStatus.PENDING)
                .returnType(request.getLines().size() == order.getLines().size()
                        ? Return.ReturnType.FULL
                        : Return.ReturnType.PARTIAL)
                .returnDate(LocalDateTime.now())
                .notes(request.getNotes())
                .refundStatus(Return.RefundStatus.PENDING)
                .build();

        // Add return lines
        for (ReturnLineRequest lineRequest : request.getLines()) {
            OrderLine orderLine = orderLineRepository.findById(lineRequest.getOrderLineId())
                    .orElseThrow(() -> new IllegalArgumentException("Order line not found: " + lineRequest.getOrderLineId()));

            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.getProductId()));

            ReturnReason returnReason = returnReasonRepository.findById(lineRequest.getReturnReasonId())
                    .orElseThrow(() -> new IllegalArgumentException("Return reason not found: " + lineRequest.getReturnReasonId()));

            // Validate quantity
            if (lineRequest.getQuantityReturned().compareTo(orderLine.getQuantity()) > 0) {
                throw new IllegalArgumentException("Cannot return more than ordered quantity");
            }

            ReturnLine returnLine = ReturnLine.builder()
                    .returnEntity(returnEntity)
                    .orderLine(orderLine)
                    .product(product)
                    .returnReason(returnReason)
                    .quantityOrdered(orderLine.getQuantity())
                    .quantityReturned(lineRequest.getQuantityReturned())
                    .quantityAccepted(BigDecimal.ZERO)
                    .quantityRejected(BigDecimal.ZERO)
                    .condition(ReturnLine.ProductCondition.UNKNOWN)
                    .unitPrice(orderLine.getUnitPrice())
                    .discountPercentage(orderLine.getDiscountPercentage())
                    .taxRate(orderLine.getTaxRate())
                    .restockEligible(returnReason.getAllowsRestock())
                    .restocked(false)
                    .notes(lineRequest.getNotes())
                    .build();

            returnLine.calculateLineTotal();
            returnEntity.addLine(returnLine);
        }

        // Calculate return totals
        calculateReturnTotals(returnEntity);

        // Save return
        Return savedReturn = returnRepository.save(returnEntity);
        log.info("Created return: {} for order: {}", savedReturn.getReturnNumber(), order.getOrderNumber());

        return toReturnResponse(savedReturn);
    }

    @Transactional
    public ReturnResponse approveReturn(UUID returnId, String notes) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getStatus() != Return.ReturnStatus.PENDING) {
            throw new IllegalStateException("Only pending returns can be approved");
        }

        returnEntity.setStatus(Return.ReturnStatus.APPROVED);
        returnEntity.setExpectedDate(LocalDateTime.now().plusDays(7)); // Expected in 7 days

        if (notes != null) {
            returnEntity.setInternalNotes(returnEntity.getInternalNotes() != null
                    ? returnEntity.getInternalNotes() + "\n" + notes
                    : notes);
        }

        Return savedReturn = returnRepository.save(returnEntity);
        log.info("Approved return: {}", savedReturn.getReturnNumber());

        return toReturnResponse(savedReturn);
    }

    @Transactional
    public ReturnResponse rejectReturn(UUID returnId, String rejectionReason) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getStatus() != Return.ReturnStatus.PENDING) {
            throw new IllegalStateException("Only pending returns can be rejected");
        }

        returnEntity.setStatus(Return.ReturnStatus.REJECTED);
        returnEntity.setRejectionReason(rejectionReason);

        Return savedReturn = returnRepository.save(returnEntity);
        log.info("Rejected return: {} - Reason: {}", savedReturn.getReturnNumber(), rejectionReason);

        return toReturnResponse(savedReturn);
    }

    @Transactional
    public ReturnResponse markAsReceived(UUID returnId) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getStatus() != Return.ReturnStatus.APPROVED &&
            returnEntity.getStatus() != Return.ReturnStatus.IN_TRANSIT) {
            throw new IllegalStateException("Return must be approved or in transit to mark as received");
        }

        returnEntity.setStatus(Return.ReturnStatus.RECEIVED);
        returnEntity.setReceivedDate(LocalDateTime.now());

        Return savedReturn = returnRepository.save(returnEntity);
        log.info("Marked return as received: {}", savedReturn.getReturnNumber());

        return toReturnResponse(savedReturn);
    }

    @Transactional
    public ReturnResponse inspectReturn(UUID returnId, List<InspectReturnLineRequest> inspections) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getStatus() != Return.ReturnStatus.RECEIVED) {
            throw new IllegalStateException("Return must be received before inspection");
        }

        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        for (InspectReturnLineRequest inspection : inspections) {
            ReturnLine returnLine = returnLineRepository.findById(inspection.getReturnLineId())
                    .orElseThrow(() -> new IllegalArgumentException("Return line not found: " + inspection.getReturnLineId()));

            // Validate quantities
            BigDecimal totalInspected = inspection.getQuantityAccepted().add(inspection.getQuantityRejected());
            if (totalInspected.compareTo(returnLine.getQuantityReturned()) != 0) {
                throw new IllegalArgumentException("Accepted + Rejected must equal returned quantity");
            }

            // Update return line
            returnLine.setCondition(ReturnLine.ProductCondition.valueOf(inspection.getCondition()));
            returnLine.setQuantityAccepted(inspection.getQuantityAccepted());
            returnLine.setQuantityRejected(inspection.getQuantityRejected());
            returnLine.setInspectionNotes(inspection.getInspectionNotes());

            // Determine if eligible for restock
            returnLine.setRestockEligible(
                    returnLine.getReturnReason().getAllowsRestock() &&
                    (returnLine.getCondition() == ReturnLine.ProductCondition.PERFECT ||
                     returnLine.getCondition() == ReturnLine.ProductCondition.GOOD) &&
                    returnLine.getQuantityAccepted().compareTo(BigDecimal.ZERO) > 0
            );

            // Set warehouse location if provided
            if (inspection.getWarehouseLocationId() != null) {
                WarehouseLocation location = warehouseLocationRepository.findById(inspection.getWarehouseLocationId())
                        .orElseThrow(() -> new IllegalArgumentException("Warehouse location not found"));
                returnLine.setWarehouseLocation(location);
            }

            // Calculate refund amount for this line
            returnLine.calculateRefundAmount();
            totalRefundAmount = totalRefundAmount.add(returnLine.getRefundAmount());

            returnLineRepository.save(returnLine);
        }

        // Update return entity
        returnEntity.setStatus(Return.ReturnStatus.INSPECTED);
        returnEntity.setInspectedDate(LocalDateTime.now());
        returnEntity.setRefundAmount(totalRefundAmount);

        Return savedReturn = returnRepository.save(returnEntity);
        log.info("Inspected return: {} - Total refund: {}", savedReturn.getReturnNumber(), totalRefundAmount);

        return toReturnResponse(savedReturn);
    }

    @Transactional
    public ReturnResponse restockReturn(UUID returnId) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getStatus() != Return.ReturnStatus.INSPECTED) {
            throw new IllegalStateException("Return must be inspected before restocking");
        }

        for (ReturnLine returnLine : returnEntity.getLines()) {
            if (returnLine.getRestockEligible() && !returnLine.getRestocked()) {
                // Add stock back to inventory
                stockService.restockFromReturn(returnLine);

                returnLine.setRestocked(true);
                returnLine.setRestockedDate(LocalDateTime.now());
                returnLineRepository.save(returnLine);

                log.info("Restocked product: {} - Quantity: {}",
                        returnLine.getProduct().getSku(),
                        returnLine.getQuantityAccepted());
            }
        }

        returnEntity.setStatus(Return.ReturnStatus.COMPLETED);
        returnEntity.setCompletedDate(LocalDateTime.now());

        Return savedReturn = returnRepository.save(returnEntity);
        log.info("Restocked return: {}", savedReturn.getReturnNumber());

        return toReturnResponse(savedReturn);
    }

    @Transactional
    public ReturnResponse processRefund(UUID returnId, ProcessRefundRequest request) {
        Return returnEntity = getReturnById(returnId);

        if (returnEntity.getStatus() != Return.ReturnStatus.COMPLETED) {
            throw new IllegalStateException("Return must be completed before processing refund");
        }

        if (returnEntity.getRefundStatus() == Return.RefundStatus.COMPLETED) {
            throw new IllegalStateException("Refund already processed");
        }

        // Validate refund amount
        if (request.getRefundAmount().compareTo(returnEntity.getRefundAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed calculated refund amount");
        }

        returnEntity.setRefundAmount(request.getRefundAmount());
        returnEntity.setRefundMethod(request.getRefundMethod());
        returnEntity.setRefundReference(request.getRefundReference());
        returnEntity.setRefundStatus(Return.RefundStatus.COMPLETED);
        returnEntity.setRefundDate(LocalDateTime.now());

        if (request.getNotes() != null) {
            returnEntity.setInternalNotes(returnEntity.getInternalNotes() != null
                    ? returnEntity.getInternalNotes() + "\n" + request.getNotes()
                    : request.getNotes());
        }

        Return savedReturn = returnRepository.save(returnEntity);
        log.info("Processed refund for return: {} - Amount: {} - Method: {}",
                savedReturn.getReturnNumber(),
                request.getRefundAmount(),
                request.getRefundMethod());

        return toReturnResponse(savedReturn);
    }

    public ReturnResponse getReturn(UUID returnId) {
        Return returnEntity = getReturnById(returnId);
        return toReturnResponse(returnEntity);
    }

    public ReturnResponse getReturnByNumber(String returnNumber) {
        Return returnEntity = returnRepository.findByReturnNumber(returnNumber)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnNumber));
        return toReturnResponse(returnEntity);
    }

    public Page<ReturnResponse> getReturns(Pageable pageable) {
        return returnRepository.findAll(pageable).map(this::toReturnResponse);
    }

    public Page<ReturnResponse> getReturnsByStatus(Return.ReturnStatus status, Pageable pageable) {
        return returnRepository.findByStatus(status, pageable).map(this::toReturnResponse);
    }

    public Page<ReturnResponse> getReturnsByCustomer(UUID customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        return returnRepository.findByCustomer(customer, pageable).map(this::toReturnResponse);
    }

    public List<ReturnReasonResponse> getActiveReturnReasons() {
        return returnReasonRepository.findByActiveTrue()
                .stream()
                .map(this::toReturnReasonResponse)
                .collect(Collectors.toList());
    }

    private Return getReturnById(UUID returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
    }

    private void calculateReturnTotals(Return returnEntity) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (ReturnLine line : returnEntity.getLines()) {
            subtotal = subtotal.add(line.getLineTotal());

            BigDecimal lineTax = line.getLineTotal()
                    .multiply(line.getTaxRate())
                    .divide(BigDecimal.valueOf(100));
            taxAmount = taxAmount.add(lineTax);
        }

        returnEntity.setSubtotalAmount(subtotal);
        returnEntity.setTaxAmount(taxAmount);
        returnEntity.setTotalAmount(subtotal.add(taxAmount));
    }

    private String generateReturnNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = returnRepository.countReturnsSince(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
        );
        return String.format("RET-%s-%04d", timestamp, count + 1);
    }

    // Mapper methods
    private ReturnResponse toReturnResponse(Return returnEntity) {
        return ReturnResponse.builder()
                .id(returnEntity.getId())
                .returnNumber(returnEntity.getReturnNumber())
                .orderId(returnEntity.getOrder().getId())
                .orderNumber(returnEntity.getOrder().getOrderNumber())
                .customerId(returnEntity.getCustomer().getId())
                .customerName(returnEntity.getCustomer().getName())
                .warehouseId(returnEntity.getWarehouse().getId())
                .warehouseName(returnEntity.getWarehouse().getName())
                .status(returnEntity.getStatus().name())
                .returnType(returnEntity.getReturnType().name())
                .returnDate(returnEntity.getReturnDate())
                .expectedDate(returnEntity.getExpectedDate())
                .receivedDate(returnEntity.getReceivedDate())
                .inspectedDate(returnEntity.getInspectedDate())
                .completedDate(returnEntity.getCompletedDate())
                .subtotalAmount(returnEntity.getSubtotalAmount())
                .taxAmount(returnEntity.getTaxAmount())
                .totalAmount(returnEntity.getTotalAmount())
                .refundAmount(returnEntity.getRefundAmount())
                .refundMethod(returnEntity.getRefundMethod())
                .refundStatus(returnEntity.getRefundStatus().name())
                .refundDate(returnEntity.getRefundDate())
                .refundReference(returnEntity.getRefundReference())
                .notes(returnEntity.getNotes())
                .internalNotes(returnEntity.getInternalNotes())
                .rejectionReason(returnEntity.getRejectionReason())
                .requestedById(returnEntity.getRequestedBy() != null ? returnEntity.getRequestedBy().getId() : null)
                .requestedByName(returnEntity.getRequestedBy() != null ? returnEntity.getRequestedBy().getUsername() : null)
                .approvedById(returnEntity.getApprovedBy() != null ? returnEntity.getApprovedBy().getId() : null)
                .approvedByName(returnEntity.getApprovedBy() != null ? returnEntity.getApprovedBy().getUsername() : null)
                .inspectedById(returnEntity.getInspectedBy() != null ? returnEntity.getInspectedBy().getId() : null)
                .inspectedByName(returnEntity.getInspectedBy() != null ? returnEntity.getInspectedBy().getUsername() : null)
                .lines(returnEntity.getLines().stream()
                        .map(this::toReturnLineResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private ReturnLineResponse toReturnLineResponse(ReturnLine returnLine) {
        return ReturnLineResponse.builder()
                .id(returnLine.getId())
                .returnId(returnLine.getReturnEntity().getId())
                .orderLineId(returnLine.getOrderLine().getId())
                .productId(returnLine.getProduct().getId())
                .productSku(returnLine.getProduct().getSku())
                .productName(returnLine.getProduct().getName())
                .returnReasonId(returnLine.getReturnReason().getId())
                .returnReasonName(returnLine.getReturnReason().getName())
                .quantityOrdered(returnLine.getQuantityOrdered())
                .quantityReturned(returnLine.getQuantityReturned())
                .quantityAccepted(returnLine.getQuantityAccepted())
                .quantityRejected(returnLine.getQuantityRejected())
                .condition(returnLine.getCondition().name())
                .unitPrice(returnLine.getUnitPrice())
                .discountPercentage(returnLine.getDiscountPercentage())
                .taxRate(returnLine.getTaxRate())
                .lineTotal(returnLine.getLineTotal())
                .refundAmount(returnLine.getRefundAmount())
                .restockEligible(returnLine.getRestockEligible())
                .restocked(returnLine.getRestocked())
                .restockedDate(returnLine.getRestockedDate())
                .warehouseLocationId(returnLine.getWarehouseLocation() != null ? returnLine.getWarehouseLocation().getId() : null)
                .notes(returnLine.getNotes())
                .inspectionNotes(returnLine.getInspectionNotes())
                .build();
    }

    private ReturnReasonResponse toReturnReasonResponse(ReturnReason returnReason) {
        return ReturnReasonResponse.builder()
                .id(returnReason.getId())
                .code(returnReason.getCode())
                .name(returnReason.getName())
                .description(returnReason.getDescription())
                .requiresInspection(returnReason.getRequiresInspection())
                .allowsRestock(returnReason.getAllowsRestock())
                .active(returnReason.getActive())
                .build();
    }
}
