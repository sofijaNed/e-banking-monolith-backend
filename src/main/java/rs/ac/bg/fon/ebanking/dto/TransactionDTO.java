package rs.ac.bg.fon.ebanking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import rs.ac.bg.fon.ebanking.entity.Account;
import rs.ac.bg.fon.ebanking.entity.complexkeys.TransactionPK;

import java.time.LocalDateTime;

@Data
public class TransactionDTO {

    private TransactionPK transactionPK;

    private Double amount;

    private LocalDateTime date;

    private String description;

    private String status;

    private String model;

    private String number;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AccountDTO senderDTO;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AccountDTO receiverDTO;
}
