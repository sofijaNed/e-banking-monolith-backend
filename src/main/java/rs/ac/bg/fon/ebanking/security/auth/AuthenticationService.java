package rs.ac.bg.fon.ebanking.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import rs.ac.bg.fon.ebanking.audit.AuditPublisher;
import rs.ac.bg.fon.ebanking.audit.AuditRepository;
import rs.ac.bg.fon.ebanking.client.ClientRepository;
import rs.ac.bg.fon.ebanking.employee.EmployeeImpl;
import rs.ac.bg.fon.ebanking.employee.EmployeeRepository;
import rs.ac.bg.fon.ebanking.user.UserRepository;
import rs.ac.bg.fon.ebanking.employee.EmployeeDTO;
import rs.ac.bg.fon.ebanking.client.Client;
import rs.ac.bg.fon.ebanking.employee.Employee;
import rs.ac.bg.fon.ebanking.user.User;
import rs.ac.bg.fon.ebanking.security.config.JwtService;
import rs.ac.bg.fon.ebanking.security.token.Token;
import rs.ac.bg.fon.ebanking.security.token.TokenRepository;
import rs.ac.bg.fon.ebanking.security.token.TokenType;
import rs.ac.bg.fon.ebanking.user.Role;
import rs.ac.bg.fon.ebanking.security.twofactorauth.OtpService;
import rs.ac.bg.fon.ebanking.client.ClientImpl;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final OtpService otpService;
    private final ClientImpl clientImpl;
    private final EmployeeImpl employeeImpl;
    private final AuditPublisher auditPublisher;


    @Value("${jwt.refresh-token-ms:604800000}")
    private long refreshTokenMs;

    @Value("${jwt.refresh-cookie-name:refresh_token}")
    private String refreshCookieName;

    @Value("${jwt.refresh-cookie-path:/}")
    private String refreshCookiePath;

    @Value("${jwt.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;

    @Value("${jwt.refresh-pepper:pepper-change-me}")
    private String refreshPepper;

    @Value("${jwt.idle-max-ms:900000}")
    private long idleMaxMs;

    @Value("${jwt.absolute-max-ms:28800000}")
    private long absoluteMaxMs;

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception {
        long t0 = System.nanoTime();
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));

            var user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(()->new BadCredentialsException("Podaci nisu validni."));

            if (Boolean.TRUE.equals(user.getTwoFactorEnabled()) && request.isUse2fa()) {
                String preAuthToken = jwtService.generatePreAuthToken(user);

                String email = resolveEmailForUser(user);

                otpService.generateAndSendOtp(user, email, "LOGIN_2FA");

                int dur = (int)((System.nanoTime() - t0)/1_000_000);
                auditPublisher.success(
                        "LOGIN_2FA_SEND",
                        user.getUsername(),
                        "USER",
                        user.getUsername(),
                        200,
                        dur,
                        "{\"sanitized\":true,\"otp_in_payload\":false}",
                        "{\"channel\":\"EMAIL\"}"
                );

                return AuthenticationResponse.builder()
                        .twoFactorRequired(true)
                        .preAuthToken(preAuthToken)
                        .message("OTP kod poslat na email.")
                        .build();
            }
//        var jwtToken = jwtService.generateToken(user);
//        var refreshToken = jwtService.generateRefreshToken(user);
            var now = new Date();
            var accessToken  = jwtService.generateAccessToken(user);
            var refreshToken = jwtService.generateRefreshToken(user, now);

//        revokeAllMemberTokens(user);
//        saveMemberToken(user,jwtToken);

            revokeAllRefreshTokens(user);
            saveRefreshHash(user, refreshToken);


            var idRoleName = resolveIdentityForAudit(user);

            int dur = (int)((System.nanoTime() - t0)/1_000_000);
            auditPublisher.success(
                    "LOGIN",
                    user.getUsername(),
                    "USER",
                    user.getUsername(),
                    200,
                    dur,
                    "{\"sanitized\":true}",
                    "{\"method\":\"password-only\"}"
            );
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .username(user.getUsername())
                    .role(idRoleName.role)
                    .message("Uspešno prijavljivanje na sistem.")
                    .build();

            } catch (BadCredentialsException e) {
                int dur = (int)((System.nanoTime() - t0)/1_000_000);
                auditPublisher.fail(
                        "LOGIN",
                        request.getUsername(),
                        "USER",
                        request.getUsername(),
                        401,
                        dur,
                        "{\"sanitized\":true}",
                        "{\"reason\":\"bad_credentials\"}"
                );
                throw e;
            } catch (Exception e) {
                int dur = (int)((System.nanoTime() - t0)/1_000_000);
                auditPublisher.fail(
                        "LOGIN",
                        request.getUsername(),
                        "USER",
                        request.getUsername(),
                        500,
                        dur,
                        "{\"sanitized\":true}",
                        "{\"reason\":\"unexpected\"}"
                );
                throw e;
    }


    }

    public Object checkForActiveUser(String token) throws Exception {
        String username = jwtService.extractUsername(token);
        if (jwtService.isTokenExpired(token)) username = null;

        if (username != null && !username.contains("employee")) {
            return clientImpl.findByUsername(username);
        } else {
            Employee employee = employeeRepository.findEmployeeByUserEmployeeUsername(username);
            return modelMapper.map(employee, EmployeeDTO.class);
        }
    }

    public AuthenticationResponse completeAuthentication(User user) {
//        var jwtToken = jwtService.generateToken(user);
//        var refreshToken = jwtService.generateRefreshToken(user);
        long t0 = System.nanoTime();
        var now = new Date();
        var accessToken  = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user, now);
//        revokeAllMemberTokens(user);
//        saveMemberToken(user, jwtToken);

        revokeAllRefreshTokens(user);
        saveRefreshHash(user, refreshToken);

        var idRoleName = resolveIdentityForAudit(user);

        int dur = (int)((System.nanoTime() - t0)/1_000_000);
        auditPublisher.success(
                "LOGIN_2FA_VERIFY",
                user.getUsername(),
                "USER",
                user.getUsername(),
                200,
                dur,
                "{\"sanitized\":true}",
                "{\"result\":\"verified\"}"
        );

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .twoFactorRequired(false)
                .role(user.getRole().name())
                .message("Uspešno potrvđen OTP kod.")
                .build();
    }


    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("In refresh token method.");
        long t0 = System.nanoTime();
        String rawRefresh = readLatestValidRefresh(request);
        if (rawRefresh == null) {
            int dur = (int)((System.nanoTime() - t0)/1_000_000);
            auditPublisher.fail("REFRESH_TOKEN", null, "USER", null, 401, dur,
                    "{\"sanitized\":true}", "{\"reason\":\"missing_cookie\"}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String typ = null;
        try {
            typ = jwtService.extractTyp(rawRefresh);
        } catch (Exception e) {
            int dur = (int)((System.nanoTime() - t0)/1_000_000);
            auditPublisher.fail("REFRESH_TOKEN", null, "USER", null, 401, dur,
                    "{\"sanitized\":true}", "{\"reason\":\"invalid_or_expired\"}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!"refresh".equals(typ) || jwtService.isTokenExpired(rawRefresh)) {
            int dur = (int)((System.nanoTime() - t0)/1_000_000);
            auditPublisher.fail("REFRESH_TOKEN", null, "USER", null, 401, dur,
                    "{\"sanitized\":true}", "{\"reason\":\"parse_error\"}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        long now = System.currentTimeMillis();
        Date iat = jwtService.extractIssuedAt(rawRefresh);
        Date ori = jwtService.extractOriginalIat(rawRefresh);
        if (iat == null || now - iat.getTime() > idleMaxMs)      { response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return; }
        if (ori == null || now - ori.getTime() > absoluteMaxMs)  { response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return; }

        String username = jwtService.extractUsername(rawRefresh);
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen."));

        String refreshHash = sha256(refreshPepper + rawRefresh);
        var storedOpt = tokenRepository.findByToken(refreshHash);
        if (storedOpt.isEmpty() || storedOpt.get().isRevoked() || storedOpt.get().isExpired()) {
            int dur = (int)((System.nanoTime() - t0)/1_000_000);
            auditPublisher.fail("REFRESH_TOKEN", username, "USER", username, 401, dur,
                    "{\"sanitized\":true}", "{\"reason\":\"revoked_or_missing\"}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        var stored = storedOpt.get();
        stored.setRevoked(true);
        stored.setExpired(true);
        tokenRepository.save(stored);

        var newAccess  = jwtService.generateAccessToken(user);
        var newRefresh = jwtService.generateRefreshToken(user, ori);
        saveRefreshHash(user, newRefresh);


        setRefreshCookie(response, newRefresh); // HttpOnly+Secure cookie
        int dur = (int)((System.nanoTime() - t0)/1_000_000);
        auditPublisher.success(
                "REFRESH_TOKEN",
                username,
                "USER",
                username,
                200,
                dur,
                "{\"sanitized\":true}",
                "{\"rotation\":\"true\"}"
        );
        var authResponse = AuthenticationResponse.builder()
                .accessToken(newAccess)
                .message("refreshed")
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    private String safeExtractTyp(String token) {
        try { return jwtService.extractTyp(token); } catch (Exception e) { return null; }
    }

    public void revokeAllRefreshTokens(User user) {
        var tokens = tokenRepository.findAllValidByUserAndType(user.getUsername(), TokenType.REFRESH);
        if (tokens.isEmpty()) return;
        tokens.forEach(t -> { t.setExpired(true); t.setRevoked(true); });
        tokenRepository.saveAll(tokens);
    }

    public void revokeSingleRefresh(User user, String rawRefreshFromCookie) {
        String h = sha256(refreshPepper + rawRefreshFromCookie);
        tokenRepository.findByToken(h).ifPresent(t -> {
            if (t.getUser().getUsername().equals(user.getUsername())) {
                t.setExpired(true); t.setRevoked(true);
                tokenRepository.save(t);
            }
        });
    }

    private void setRefreshCookie(HttpServletResponse response, String rawRefresh) {
        deleteRefreshCookie(response, "/auth");
        if (!"/".equals(refreshCookiePath)) {
            deleteRefreshCookie(response, "/");
        }

        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, rawRefresh)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Strict")
                .path(refreshCookiePath)       // << "/"
                .maxAge(Duration.ofMillis(refreshTokenMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteRefreshCookie(HttpServletResponse response, String path) {
        ResponseCookie del = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path(path)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, del.toString());
    }

    private String readCookie(HttpServletRequest req, String name) {
        Cookie[] cs = Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]);
        return Arrays.stream(cs).filter(c -> name.equals(c.getName())).map(Cookie::getValue).findFirst().orElse(null);
    }

    private void saveRefreshHash(User user, String rawRefresh) {
        var token = Token.builder()
                .user(user)
                .token(sha256(refreshPepper + rawRefresh))
                .tokenType(TokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 nije dostupan", e);
        }
    }

    private static class Identity {
        Long id; String role; String name;
        Identity(Long id, String role, String name) { this.id=id; this.role=role; this.name=name; }
    }

    private Identity resolveIdentityForAudit(User user) {
        Long id; String role; String name;
        if (user.getRole() == Role.ROLE_CLIENT) {
            Client c = clientRepository.findClientByUserClientUsername(user.getUsername());
            id = c.getId(); role = Role.ROLE_CLIENT.name(); name = c.getFirstname();
        } else {
            Employee e = employeeRepository.findEmployeeByUserEmployeeUsername(user.getUsername());
            id = e.getId(); role = Role.ROLE_EMPLOYEE.name(); name = e.getFirstname();
        }
        return new Identity(id, role, name);
    }

    private String readLatestValidRefresh(HttpServletRequest req) {
        Cookie[] cs = Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]);

        String best = null;
        Date bestIat = null;

        for (Cookie c : cs) {
            if (!refreshCookieName.equals(c.getName())) continue;
            String val = c.getValue();
            try {
                if (!"refresh".equals(jwtService.extractTyp(val))) continue;
                if (jwtService.isTokenExpired(val)) continue;
                Date iat = jwtService.extractIssuedAt(val);
                if (best == null || (iat != null && iat.after(bestIat))) {
                    best = val;
                    bestIat = iat;
                }
            } catch (Exception ignored) {}
        }
        return best;
    }

    public void revokeAllMemberTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUsername());
        System.out.println(validUserTokens);
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }


    public void saveMemberToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private String resolveEmailForUser(User user) throws Exception {
        if (user.getRole() == Role.ROLE_CLIENT) {
            var client = Optional.ofNullable(clientImpl.findByUsername(user.getUsername()))
                    .orElseThrow(() -> new UsernameNotFoundException("Klijent nije pronađen."));

            return Optional.ofNullable(client.getEmail())
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> new UsernameNotFoundException("Email nije postavljen."));
        } else {
            var employee = Optional.ofNullable(employeeImpl.findByUsername(user.getUsername()))
                    .orElseThrow(() -> new UsernameNotFoundException("Zaposleni nije pronađen."));

            return Optional.ofNullable(employee.getEmail())
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> new UsernameNotFoundException("Email nije postavljen."));
        }
    }
}
