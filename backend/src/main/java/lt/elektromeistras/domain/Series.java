package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Series (Serija) - Static dimension
 * Represents document series or sequence types for grouping transactions
 */
@Entity
@Table(name = "series", indexes = {
    @Index(name = "idx_series_code", columnList = "code", unique = true),
    @Index(name = "idx_series_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Series extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "series_type", nullable = false)
    private SeriesType seriesType;

    @Column(name = "prefix", length = 20)
    private String prefix;

    @Column(name = "current_number")
    private Long currentNumber;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public enum SeriesType {
        INVOICE,      // Sąskaita faktūra
        ORDER,        // Užsakymas
        PAYMENT,      // Mokėjimas
        DOCUMENT,     // Dokumentas
        JOURNAL       // Žurnalas
    }
}
