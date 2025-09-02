package rs.ac.bg.fon.ebanking.loan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import rs.ac.bg.fon.ebanking.account.Account;
import rs.ac.bg.fon.ebanking.audit.Auditable;
import rs.ac.bg.fon.ebanking.employee.Employee;
import rs.ac.bg.fon.ebanking.loanpayment.LoanPayment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Loan extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "principal_amount", precision=19, scale=4)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", precision=19, scale=4)
    private BigDecimal interestRate;

    @Column(name = "term_months")
    private Integer termMonths;

    @Column(name = "currency")
    private String currency;

    @Column(name = "note")
    private String note;

    @Column(name = "date_issued")
    private LocalDate dateIssued;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LoanStatus status;

    @Column(name = "monthly_payment", precision=19, scale=4)
    private BigDecimal monthlyPayment;

    @Column(name = "outstanding_balance", precision=19, scale=4)
    private BigDecimal outstandingBalance;

    @JoinColumn(name="account_id",referencedColumnName = "id")
    @ManyToOne(optional = false)
    @JsonIgnore
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="approved_by")
    private Employee approvedBy;

    @Column(name = "approved_at")
    private LocalDate approvedAt;

    @OneToMany(mappedBy="loan", cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<LoanPayment> payments = new HashSet<>();
}
