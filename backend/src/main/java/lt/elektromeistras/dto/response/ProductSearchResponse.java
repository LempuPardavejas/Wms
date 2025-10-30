package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight DTO for product search autocomplete
 * Contains only essential fields for fast transfer and display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {

    private UUID id;
    private String code;
    private String sku;
    private String name;
    private String unitOfMeasure;
    private BigDecimal basePrice;
    private Boolean isCable;
    private Boolean isModular;
    private String imageUrl;

    /**
     * Display label for autocomplete: "0010006 - Kabelis YDYP 3x1.5"
     */
    public String getLabel() {
        return code + " - " + name;
    }

    /**
     * Secondary text for autocomplete: "€1.25 / M"
     */
    public String getSecondaryText() {
        return String.format("€%.2f / %s", basePrice, unitOfMeasure);
    }
}
