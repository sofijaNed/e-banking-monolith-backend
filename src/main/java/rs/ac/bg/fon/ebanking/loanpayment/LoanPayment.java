package rs.ac.bg.fon.ebanking.loanpayment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import rs.ac.bg.fon.ebanking.audit.Auditable;
import rs.ac.bg.fon.ebanking.loan.Loan;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoanPayment extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "paid", nullable = false)
    private boolean paid = false;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(name = "principal_amount", precision = 19, scale = 4)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 19, scale = 4)
    private BigDecimal interestAmount;

    @Column(name = "note")
    private String note;
}