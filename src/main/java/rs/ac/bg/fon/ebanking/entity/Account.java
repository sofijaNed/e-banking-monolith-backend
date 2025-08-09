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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="clientid")
    @JsonIgnore
    private Client client;
}
