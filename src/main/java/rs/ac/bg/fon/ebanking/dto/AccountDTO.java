package rs.ac.bg.fon.ebanking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import rs.ac.bg.fon.ebanking.entity.AccountType;
import rs.ac.bg.fon.ebanking.entity.Currency;


import java.time.LocalDate;
import java.util.Collection;

@Data
public class AccountDTO {
    private String id;

    private AccountType type;

    private Double balance;

    private Currency currency;

    private LocalDate opened;

    //@JsonInclude(JsonInclude.Include.NON_NULL)
    private ClientDTO clientDTO;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<TransactionDTO> sentTransactions;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<TransactionDTO> receivedTransactions;
}
