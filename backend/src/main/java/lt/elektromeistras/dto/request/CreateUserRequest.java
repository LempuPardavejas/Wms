package lt.elektromeistras.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Create user request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Vartotojo vardas yra privalomas")
    @Size(min = 3, max = 100, message = "Vartotojo vardas turi būti nuo 3 iki 100 simbolių")
    private String username;

    @NotBlank(message = "El. paštas yra privalomas")
    @Email(message = "Neteisingas el. pašto formatas")
    private String email;

    @NotBlank(message = "Slaptažodis yra privalomas")
    @Size(min = 6, message = "Slaptažodis turi būti bent 6 simbolių ilgio")
    private String password;

    private String firstName;
    private String lastName;
    private String phone;

    private Set<String> roleCodes;
}
