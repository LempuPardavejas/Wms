package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Person (Asmuo) - Static dimension
 * Represents persons responsible for transactions, projects, or departments
 * Can be linked to system users or external persons
 */
@Entity
@Table(name = "persons", indexes = {
    @Index(name = "idx_person_code", columnList = "code", unique = true),
    @Index(name = "idx_person_active", columnList = "is_active"),
    @Index(name = "idx_person_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Person extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false)
    private PersonType personType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "position", length = 200)
    private String position;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public enum PersonType {
        EMPLOYEE,        // Darbuotojas
        MANAGER,         // Vadovas
        ACCOUNTANT,      // Buhalteris
        SALES_PERSON,    // Pardavėjas
        EXTERNAL         // Išorinis asmuo
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
