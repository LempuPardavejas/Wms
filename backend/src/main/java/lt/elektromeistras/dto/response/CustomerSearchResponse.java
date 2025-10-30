package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight DTO for customer search autocomplete
 * Contains only essential fields for fast transfer and display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchResponse {

    private UUID id;
    private String code;
    private String customerType;
    private String companyName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String city;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;

    /**
     * Display name for the customer
     */
    public String getDisplayName() {
        if ("BUSINESS".equals(customerType) || "CONTRACTOR".equals(customerType)) {
            return companyName != null ? companyName : code;
        } else {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            } else if (lastName != null) {
                return lastName;
            } else {
                return code;
            }
        }
    }

    /**
     * Display label for autocomplete: "B001 - UAB Elektros Darbai"
     */
    public String getLabel() {
        return code + " - " + getDisplayName();
    }

    /**
     * Secondary text for autocomplete: "Vilnius | €5000 credit"
     */
    public String getSecondaryText() {
        StringBuilder sb = new StringBuilder();
        if (city != null) {
            sb.append(city);
        }
        if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) > 0) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(String.format("€%.0f credit", creditLimit));
        }
        return sb.toString();
    }
}
