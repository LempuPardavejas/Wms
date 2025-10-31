package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Vartotojo vardas arba el. paštas yra privalomas")
    private String usernameOrEmail;

    @NotBlank(message = "Slaptažodis yra privalomas")
    private String password;

    private Boolean rememberMe = false;
}
