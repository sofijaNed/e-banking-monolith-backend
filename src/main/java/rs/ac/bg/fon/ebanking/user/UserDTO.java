package rs.ac.bg.fon.ebanking.user;

import lombok.Data;

@Data
public class UserDTO {

    private String username;

    private String password;

    private Role role;
}
