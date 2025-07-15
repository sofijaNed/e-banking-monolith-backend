package rs.ac.bg.fon.ebanking.security.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    /**
     * Primary key of entity that connects to user
     */
    private Integer id;

    /**
     * Represent JWT access token of user.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accessToken;

    /**
     * Represent JWT refresh token of user
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String refreshToken;

    /**
     * Represent message for user
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String role;

}
