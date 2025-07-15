package rs.ac.bg.fon.ebanking.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import rs.ac.bg.fon.ebanking.entity.complexkeys.TransactionPK;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction implements Serializable {

    @EmbeddedId
    private TransactionPK transactionPK;

    @Column(name="amount")
    private Double amount;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "model")
    private String model;

    @Column(name = "number")
    private String number;

    @JoinColumn(name="sender",referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account sender;

    @JoinColumn(name="receiver", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account receiver;
}
