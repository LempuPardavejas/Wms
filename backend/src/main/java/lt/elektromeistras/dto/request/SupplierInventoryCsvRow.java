package lt.elektromeistras.dto.request;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing a row from FORMAPAK supplier inventory CSV file
 */
@Data
public class SupplierInventoryCsvRow {

    @CsvBindByName(column = "Padalinio kodas")
    private String divisionCode;

    @CsvBindByName(column = "Padalinys")
    private String division;

    @CsvBindByName(column = "Padalinio Nr.")
    private String divisionNumber;

    @CsvBindByName(column = "Grupės kodas")
    private String categoryCode;

    @CsvBindByName(column = "Grupės pavadinimas")
    private String categoryName;

    @CsvBindByName(column = "Balanso sąskaita")
    private String balanceAccount;

    @CsvBindByName(column = "PVM %")
    private BigDecimal vatPercent;

    @CsvBindByName(column = "Pogrupio kodas")
    private String productCode;

    @CsvBindByName(column = "Pogrupio pavadinimas")
    private String productName;

    @CsvBindByName(column = "Mat.vnt")
    private String unitOfMeasure;

    @CsvBindByName(column = "Brūkšninis kodas")
    private String barcode;

    @CsvBindByName(column = "Specifikacija")
    private String specification;

    @CsvBindByName(column = "Pajamavimo data")
    private String receivingDate; // Will be parsed to LocalDate

    @CsvBindByName(column = "Pajamavimo kaina EUR")
    private BigDecimal receivingPrice;

    @CsvBindByName(column = "Didmeninė kaina")
    private BigDecimal wholesalePrice;

    @CsvBindByName(column = "Mažmeninė kaina")
    private BigDecimal retailPrice;

    @CsvBindByName(column = "Kiekis")
    private BigDecimal quantity;

    @CsvBindByName(column = "Suma EUR")
    private BigDecimal totalAmount;

    @CsvBindByName(column = "Tiekėjo kodas")
    private String supplierCode;

    @CsvBindByName(column = "Tiekėjo pavadinimas")
    private String supplierName;

    /**
     * Parse receiving date from string format (yyyy.MM.dd)
     */
    public LocalDate getParsedReceivingDate() {
        if (receivingDate == null || receivingDate.trim().isEmpty()) {
            return null;
        }
        try {
            // Format: 2018.12.20
            String[] parts = receivingDate.trim().split("\\.");
            if (parts.length == 3) {
                return LocalDate.of(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
                );
            }
        } catch (Exception e) {
            // Invalid date format
        }
        return null;
    }

    /**
     * Clean and trim string fields
     */
    public void cleanData() {
        productCode = cleanString(productCode);
        productName = cleanString(productName);
        categoryCode = cleanString(categoryCode);
        categoryName = cleanString(categoryName);
        unitOfMeasure = cleanString(unitOfMeasure);
        barcode = cleanString(barcode);
        supplierCode = cleanString(supplierCode);
        supplierName = cleanString(supplierName);
        divisionCode = cleanString(divisionCode);
        division = cleanString(division);
    }

    private String cleanString(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * Validate required fields
     */
    public boolean isValid() {
        return productCode != null && !productCode.isEmpty()
            && productName != null && !productName.isEmpty()
            && quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0;
    }
}
