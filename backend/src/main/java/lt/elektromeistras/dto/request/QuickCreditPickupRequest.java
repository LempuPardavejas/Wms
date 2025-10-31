package lt.elektromeistras.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Ultra-fast credit pickup request using customer and product codes
 * Optimized for rapid entry similar to the existing QuickOrderRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickCreditPickupRequest {

    private String customerCode;

    private List<QuickCreditLineItem> items = new ArrayList<>();

    private String performedBy;

    private String performedByRole; // CUSTOMER, EMPLOYEE, ADMINISTRATOR

    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickCreditLineItem {
        private String productCode;
        private BigDecimal quantity;
        private String notes;
    }
}
