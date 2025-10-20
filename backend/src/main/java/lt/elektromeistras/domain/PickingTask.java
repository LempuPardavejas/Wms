package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "picking_tasks", indexes = {
    @Index(name = "idx_task_wave", columnList = "wave_id"),
    @Index(name = "idx_task_order", columnList = "order_id"),
    @Index(name = "idx_task_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickingTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wave_id")
    private PickingWave wave;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private WarehouseLocation location;

    @Column(name = "quantity_to_pick", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityToPick;

    @Column(name = "quantity_picked", precision = 12, scale = 3)
    private BigDecimal quantityPicked = BigDecimal.ZERO;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        SKIPPED
    }
}
