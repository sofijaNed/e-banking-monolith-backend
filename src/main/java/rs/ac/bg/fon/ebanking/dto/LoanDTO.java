package rs.ac.bg.fon.ebanking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import rs.ac.bg.fon.ebanking.entity.Client;

import java.time.LocalDate;

@Data
public class LoanDTO {
    private Integer id;

    private Double principal_amount;

    private Double interest_rate;

    private LocalDate loan_term;

    private LocalDate date_issued;

    private Double monthly_payment;

    private Double outstanding_balance;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ClientDTO clientDTO;
}
