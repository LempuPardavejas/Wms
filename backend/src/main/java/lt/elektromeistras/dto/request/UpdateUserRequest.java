package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Update user request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Email(message = "Neteisingas el. pašto formatas")
    private String email;

    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;

    private Set<String> roleCodes;
}
