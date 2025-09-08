package rs.ac.bg.fon.ebanking.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import rs.ac.bg.fon.ebanking.audit.Auditable;
import rs.ac.bg.fon.ebanking.user.User;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Client extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name="id")
    private Long id;


    @Column(name = "firstname", nullable = false)
    private String firstname;


    @Column(name = "lastname", nullable = false)
    private String lastname;


    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;


    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "jmbg", nullable = false, length = 13, unique = true)
    private String jmbg;

    @Column(name = "id_card_no", length = 20)
    private String idCardNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", referencedColumnName = "username")
    @JsonIgnore
    private User userClient;

//    @OneToMany(mappedBy = "client")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
//    private Collection<Account> accounts;
//
//    @OneToMany(mappedBy = "client")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
//    private Collection<Loan> loans;


}
