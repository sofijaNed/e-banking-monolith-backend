package rs.ac.bg.fon.ebanking.security.registration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationTicketDTO {
    private String ticketId;
    private String emailMasked;
}
