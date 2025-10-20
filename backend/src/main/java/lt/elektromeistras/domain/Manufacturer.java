package lt.elektromeistras.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "manufacturers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manufacturer extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String website;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
