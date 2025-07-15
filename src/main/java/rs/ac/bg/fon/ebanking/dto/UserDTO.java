package rs.ac.bg.fon.ebanking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import rs.ac.bg.fon.ebanking.entity.Role;

@Data
public class UserDTO {

    private String username;

    private String password;

    private Role role;
}
