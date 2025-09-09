package rs.ac.bg.fon.ebanking.security.auth;

import jakarta.validation.constraints.Email;
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

    @NotBlank(message = "Korisničko ime je obavezno!")
    private String username;

    @NotBlank(message = "Lozinka je obavezna!")
    private String password;

    private boolean use2fa;

    @Email
    private String email;
}
