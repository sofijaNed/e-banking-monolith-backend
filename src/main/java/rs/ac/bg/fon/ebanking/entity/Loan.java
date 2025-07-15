package rs.ac.bg.fon.ebanking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name="loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name = "principal_amount")
    private Double principal_amount;

    @Column(name = "interest_rate")
    private Double interest_rate;

    @Column(name = "loan_term")
    private LocalDate loan_term;

    @Column(name = "date_issued")
    private LocalDate date_issued;

    @Column(name = "monthly_payment")
    private Double monthly_payment;

    @Column(name = "outstanding_balance")
    private Double outstanding_balance;

    @JoinColumn(name="clientid",referencedColumnName = "id")
    @ManyToOne(optional = false)
    @JsonIgnore
    private Client client;
}
