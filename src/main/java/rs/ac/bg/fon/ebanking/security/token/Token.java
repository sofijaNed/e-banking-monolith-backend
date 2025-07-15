package rs.ac.bg.fon.ebanking.security.token;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.*;
import rs.ac.bg.fon.ebanking.entity.User;

@Entity
@Table(name="token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;


    @Column(name="token",unique = true)
    public String token;


    @Enumerated(EnumType.STRING)
    @Column(name="type")
    public TokenType tokenType = TokenType.BEARER;


    public boolean revoked;


    public boolean expired;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user",referencedColumnName = "username")
    public User user;
}
