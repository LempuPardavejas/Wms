package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcessRefundRequest {
    @NotNull
    @Positive
    private BigDecimal refundAmount;

    @NotBlank
    private String refundMethod;

    private String refundReference;
    private String notes;
}
