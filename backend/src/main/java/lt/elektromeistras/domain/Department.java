package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Department (Padalinys) - Static dimension
 * Represents organizational units within the company
 */
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_department_code", columnList = "code", unique = true),
    @Index(name = "idx_department_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
