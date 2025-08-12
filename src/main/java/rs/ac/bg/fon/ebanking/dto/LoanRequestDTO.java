package rs.ac.bg.fon.ebanking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequestDTO {
    private BigDecimal amount;
    private Integer termMonths;
    private BigDecimal interestRate;
    private String currency;
    private String purpose;
}
