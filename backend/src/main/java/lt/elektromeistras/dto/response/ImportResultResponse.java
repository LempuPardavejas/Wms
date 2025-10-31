package lt.elektromeistras.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for supplier inventory import results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;

    private Integer totalRows;
    private Integer processedRows;
    private Integer createdProducts;
    private Integer updatedProducts;
    private Integer createdStock;
    private Integer updatedStock;
    private Integer skippedRows;
    private Integer errorRows;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    private String status; // SUCCESS, PARTIAL, FAILED

    public void addError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }

    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
    }

    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    public void determineStatus() {
        if (errorRows != null && errorRows > 0) {
            if (processedRows != null && processedRows > 0) {
                status = "PARTIAL";
            } else {
                status = "FAILED";
            }
        } else {
            status = "SUCCESS";
        }
    }
}
