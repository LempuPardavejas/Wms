package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class JournalEntryLineRequest {
    @NotNull
    private UUID glAccountId;

    @NotNull
    private BigDecimal debitAmount;

    @NotNull
    private BigDecimal creditAmount;

    private String description;

    // Static dimensions
    private UUID departmentId;
    private UUID businessObjectId;
    private UUID costCenterId;
    private UUID seriesId;
    private UUID personId;

    // Dynamic dimensions
    private UUID dimension1Id;
    private UUID dimension2Id;
    private UUID dimension3Id;
    private UUID dimension4Id;
    private UUID dimension5Id;
    private UUID dimension6Id;
    private UUID dimension7Id;
    private UUID dimension8Id;
    private UUID dimension9Id;
    private UUID dimension10Id;
    private UUID dimension11Id;
    private UUID dimension12Id;
    private UUID dimension13Id;
    private UUID dimension14Id;
    private UUID dimension15Id;

    private String notes;
}
