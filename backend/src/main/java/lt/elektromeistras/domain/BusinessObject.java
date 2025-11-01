package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Business Object (Objektas) - Static dimension
 * Represents business objects like projects, contracts, or other significant business entities
 */
@Entity
@Table(name = "business_objects", indexes = {
    @Index(name = "idx_business_object_code", columnList = "code", unique = true),
    @Index(name = "idx_business_object_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessObject extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false)
    private ObjectType objectType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public enum ObjectType {
        PROJECT,     // Projektas
        CONTRACT,    // Sutartis
        ASSET,       // Turtas
        INITIATIVE,  // Iniciatyva
        OTHER        // Kita
    }
}
