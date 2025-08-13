package rs.ac.bg.fon.ebanking.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import rs.ac.bg.fon.ebanking.user.UserDTO;

@Data
public class EmployeeDTO {

    private Long id;

    private String firstname;

    private String lastname;

    private String email;

    private String phone;

    private String position;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserDTO userEmployee;
}
