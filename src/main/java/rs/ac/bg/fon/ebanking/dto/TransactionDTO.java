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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {

    private Long id;
    private String sender;
    private String receiver;
    private BigDecimal amount;
    private String currency;
    private String model;
    private String number;
    private LocalDateTime date;
    private String description;
    private String status;
    private String reference;
    private String type;

}
