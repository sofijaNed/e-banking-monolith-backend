package rs.ac.bg.fon.ebanking.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import rs.ac.bg.fon.ebanking.audit.Auditable;
import rs.ac.bg.fon.ebanking.client.Client;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Account extends Auditable implements Serializable  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private AccountType type;

    @Column(name = "iban")
    private String iban;

    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "available_balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;

    @Column(name = "opened")
    private LocalDate opened;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="client_id")
    @JsonIgnore
    private Client client;
}
