package rs.ac.bg.fon.ebanking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private AccountType type;

    @Column(name="balance")
    private Double balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    private Currency currency;

    @Column(name = "opened")
    private LocalDate opened;

    @JoinColumn(name="clientid",referencedColumnName = "id")
    //@ManyToOne(optional = false)
    @ManyToOne
    @JsonIgnore
    private Client client;

    @OneToMany(mappedBy = "sender")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Collection<Transaction> sentTransactions;

    @OneToMany(mappedBy = "receiver")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Collection<Transaction> receivedTransactions;

}
