package lt.elektromeistras.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PersonResponse {
    private UUID id;
    private String code;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String personType;
    private UUID departmentId;
    private String position;
    private Boolean isActive;
}
