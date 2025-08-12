package rs.ac.bg.fon.ebanking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import rs.ac.bg.fon.ebanking.entity.AccountType;
import rs.ac.bg.fon.ebanking.entity.Currency;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

@Data
public class AccountDTO {

    private Long id;
    private String accountNumber;
    private String type;
    private String iban;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String currency;
    private LocalDate opened;
    private Long client;

}
