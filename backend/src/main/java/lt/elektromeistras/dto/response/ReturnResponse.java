package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    private UUID id;
    private String returnNumber;
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private String customerName;
    private UUID warehouseId;
    private String warehouseName;
    private String status;
    private String returnType;
    private LocalDateTime returnDate;
    private LocalDateTime expectedDate;
    private LocalDateTime receivedDate;
    private LocalDateTime inspectedDate;
    private LocalDateTime completedDate;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal refundAmount;
    private String refundMethod;
    private String refundStatus;
    private LocalDateTime refundDate;
    private String refundReference;
    private String notes;
    private String internalNotes;
    private String rejectionReason;
    private UUID requestedById;
    private String requestedByName;
    private UUID approvedById;
    private String approvedByName;
    private UUID inspectedById;
    private String inspectedByName;
    private List<ReturnLineResponse> lines;
}
