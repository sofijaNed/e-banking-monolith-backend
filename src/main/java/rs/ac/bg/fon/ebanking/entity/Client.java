package rs.ac.bg.fon.ebanking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name="id")
    private Integer id;


    @Column(name = "firstname")
    private String firstname;


    @Column(name = "lastname")
    private String lastname;


    @Column(name = "birthdate")
    private LocalDate birthdate;


    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

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
