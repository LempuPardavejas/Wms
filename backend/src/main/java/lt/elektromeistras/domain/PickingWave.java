package lt.elektromeistras.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "picking_waves", indexes = {
    @Index(name = "idx_wave_number", columnList = "wave_number"),
    @Index(name = "idx_wave_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_wave_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickingWave extends BaseEntity {

    @Column(name = "wave_number", nullable = false, unique = true, length = 50)
    private String waveNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WaveStatus status = WaveStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "route_json", columnDefinition = "jsonb")
    private JsonNode routeJson;

    @OneToMany(mappedBy = "wave", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PickingTask> tasks = new ArrayList<>();

    public enum WaveStatus {
        CREATED,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
