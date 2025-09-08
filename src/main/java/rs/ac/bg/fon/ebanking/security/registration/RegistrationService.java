package rs.ac.bg.fon.ebanking.security.registration;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.bg.fon.ebanking.client.Client;
import rs.ac.bg.fon.ebanking.client.ClientRepository;
import rs.ac.bg.fon.ebanking.security.twofactorauth.EmailService;
import rs.ac.bg.fon.ebanking.security.twofactorauth.UserOtp;
import rs.ac.bg.fon.ebanking.security.twofactorauth.UserOtpRepository;
import rs.ac.bg.fon.ebanking.user.Role;
import rs.ac.bg.fon.ebanking.user.User;
import rs.ac.bg.fon.ebanking.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final ClientRepository clientRepo;
    private final UserRepository userRepo;
    private final UserOtpRepository otpRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RegistrationTicketDTO request(RegistrationRequestDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw bad("Lozinke se ne poklapaju");
        }
        if (userRepo.existsByUsername(dto.getUsername())) {
            throw conflict("Korisničko ime je zauzeto");
        }
        if (!Jmbg.isValid(dto.getJmbg())) {
            throw bad("Neispravan JMBG");
        }

        Client c = clientRepo.findByJmbg(dto.getJmbg())
                .orElseThrow(() -> notFound("Klijent sa datim JMBG ne postoji"));
        if (c.getUserClient() != null) {
            throw conflict("Klijent je već registrovan");
        }
        if (!c.getFirstname().equalsIgnoreCase(dto.getFirstName())
                || !c.getLastname().equalsIgnoreCase(dto.getLastName())) {
            throw bad("Ime/prezime se ne poklapaju");
        }
        if (dto.getIdCardNo() != null && c.getIdCardNo() != null
                && !c.getIdCardNo().equalsIgnoreCase(dto.getIdCardNo())) {
            throw bad("Broj lične karte ne odgovara");
        }

        String email = Optional.ofNullable(c.getEmail())
                .orElseThrow(() -> bad("Klijent nema email u evidenciji"));

        String ticketId = UUID.randomUUID().toString();
        String rawOtp = RandomStringUtils.randomNumeric(6);
        String otpHash = passwordEncoder.encode(rawOtp);

        UserOtp otp = new UserOtp();
        otp.setUser(null);
        otp.setEmail(email.toLowerCase(Locale.ROOT));
        otp.setClientId(c.getId());
        otp.setTicketId(ticketId);
        otp.setReservedUsername(dto.getUsername());
        otp.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        otp.setOtpHash(otpHash);
        otp.setPurpose("REGISTRATION");
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setUsed(false);
        otp.setAttempts(0);
        otpRepo.save(otp);

        emailService.sendOtp(email, rawOtp);

        return new RegistrationTicketDTO(ticketId, maskEmail(email));
    }

    public void verify(RegistrationVerifyDTO dto) {
        var now = LocalDateTime.now();
        UserOtp otp = otpRepo.findFirstByPurposeAndTicketIdAndUsedFalseAndExpiresAtAfter(
                        "REGISTRATION", dto.getTicketId(), now)
                .orElseThrow(() -> bad("Neispravan ili istekao kod (ticket)"));

        if (otp.getAttempts() >= 5) throw bad("Previše pokušaja");
        if (!passwordEncoder.matches(dto.getOtpCode(), otp.getOtpHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepo.save(otp);
            throw bad("Pogrešan OTP");
        }

        if (userRepo.existsByUsername(otp.getReservedUsername())) {
            throw conflict("Korisničko ime je zauzeto");
        }
        User u = new User();
        u.setUsername(otp.getReservedUsername());
        u.setPassword(otp.getPasswordHash()); // već bcrypt
        u.setRole(Role.ROLE_CLIENT);
        u.setTwoFactorEnabled(false);
        u.setTwoFactorMethod("EMAIL");
        userRepo.save(u);

        Client c = clientRepo.findById(otp.getClientId())
                .orElseThrow(() -> bad("Klijent nije pronađen"));
        c.setUserClient(u);
        clientRepo.save(c);

        otp.setUsed(true);
        otp.setUser(u);
        otpRepo.save(otp);
    }

    private static ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }
    private static ResponseStatusException notFound(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }
    private static ResponseStatusException conflict(String m) {
        return new ResponseStatusException(HttpStatus.CONFLICT, m);
    }
    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
