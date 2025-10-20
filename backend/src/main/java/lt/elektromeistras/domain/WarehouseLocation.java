package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouse_locations", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "code"}),
    indexes = {
        @Index(name = "idx_location_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_location_zone", columnList = "zone")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 50)
    private String zone;

    @Column(length = 10)
    private String aisle;

    @Column(length = 10)
    private String rack;

    @Column
    private Integer level;
}
