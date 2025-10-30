package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReturnStatusRequest {
    @NotNull
    private String status;

    private String notes;
    private String rejectionReason;
}
