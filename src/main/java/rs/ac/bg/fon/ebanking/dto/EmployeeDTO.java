package rs.ac.bg.fon.ebanking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import rs.ac.bg.fon.ebanking.entity.User;

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
