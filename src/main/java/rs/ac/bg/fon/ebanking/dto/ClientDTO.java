package rs.ac.bg.fon.ebanking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import rs.ac.bg.fon.ebanking.entity.Account;
import rs.ac.bg.fon.ebanking.entity.Loan;
import rs.ac.bg.fon.ebanking.entity.User;

import java.time.LocalDate;
import java.util.Collection;

@Data
public class ClientDTO {
    private Integer id;

    private String firstname;

    private String lastname;

    private LocalDate birthdate;

    private String email;

    private String phone;

    private String address;

    private UserDTO userClient;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<AccountDTO> accountDTOS;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<LoanDTO> loanDTOS;
}
