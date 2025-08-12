package rs.ac.bg.fon.ebanking.security.twofactorauth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    private String preAuthToken;
    private String otpCode;
}