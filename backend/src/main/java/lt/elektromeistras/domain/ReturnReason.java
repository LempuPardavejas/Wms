package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "return_reasons", indexes = {
    @Index(name = "idx_return_reason_code", columnList = "code"),
    @Index(name = "idx_return_reason_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnReason extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "requires_inspection", nullable = false)
    private Boolean requiresInspection = false;

    @Column(name = "allows_restock", nullable = false)
    private Boolean allowsRestock = true;

    @Column(nullable = false)
    private Boolean active = true;
}
