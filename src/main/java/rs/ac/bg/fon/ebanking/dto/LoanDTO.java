package rs.ac.bg.fon.ebanking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import rs.ac.bg.fon.ebanking.entity.Client;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanDTO {
    private Long id;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String currency;
    private String note;
    private LocalDate dateIssued;
    private BigDecimal monthlyPayment;
    private BigDecimal outstandingBalance;
    private Long account;
    private Long approvedBy;
    private LocalDate approvedAt;
}
