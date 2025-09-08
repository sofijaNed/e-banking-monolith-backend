package rs.ac.bg.fon.ebanking.security.twofactorauth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.ac.bg.fon.ebanking.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_otp")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "username", referencedColumnName = "username")
    @JsonIgnore
    private User user;

    private String otpHash;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean used;
    private int attempts;
    @Column(name = "purpose", length = 50, nullable = false)
    private String purpose;
    @Column(name = "email")
    private String email;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "ticket_id", length = 64)
    private String ticketId;

    @Column(name = "reserved_username", length = 50)
    private String reservedUsername;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

}
