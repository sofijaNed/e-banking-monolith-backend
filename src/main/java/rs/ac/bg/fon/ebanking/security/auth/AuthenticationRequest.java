package rs.ac.bg.fon.ebanking.security.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @NotBlank(message = "Lozinka je obavezna")
    private String username;


    @NotBlank(message = "Lozinka je obavezna")
    private String password;
}
