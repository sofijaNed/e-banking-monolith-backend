package rs.ac.bg.fon.ebanking.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.bg.fon.ebanking.user.UserRepository;
import rs.ac.bg.fon.ebanking.user.User;
import rs.ac.bg.fon.ebanking.security.config.JwtService;
import rs.ac.bg.fon.ebanking.security.twofactorauth.OtpService;
import rs.ac.bg.fon.ebanking.security.twofactorauth.VerifyOtpRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-cookie-name:refresh_token}")
    private String refreshCookieName;

    @Value("${jwt.refresh-cookie-path:/auth}")
    private String refreshCookiePath;

    @Value("${jwt.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;

    @Value("${jwt.refresh-token-ms:604800000}")
    private long refreshTokenMs;

    private void setRefreshCookie(HttpServletResponse response, String rawRefresh) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, rawRefresh)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path(refreshCookiePath)
                .maxAge(Duration.ofMillis(refreshTokenMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

//    @GetMapping("/me/{token}")
//    public ResponseEntity<AuthenticationResponse> getCurrentUser(@PathVariable("token") String token) throws Exception {
//
//        String username = jwtService.extractUsername(token);
//        if (username == null || jwtService.isTokenExpired(token)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        Optional<User> user = userRepository.findByUsername(username);
//        if (user.isPresent()) {
//
//            return ResponseEntity.ok(AuthenticationResponse.builder()
//                    .username(user.get().getUsername())
//                    .role(user.get().getRole().name())
//                    .build());
//        }
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticationResponse> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);

        String username;
        try {
            username = jwtService.extractUsername(token);
            if (jwtService.isTokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .username(user.get().getUsername())
                        .role(user.get().getRole().name())
                        .build()
        );
    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authentication(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletResponse response
    ) {
        AuthenticationResponse auth = authenticationService.authenticate(request);

        if (!Boolean.TRUE.equals(auth.isTwoFactorRequired()) && auth.getRefreshToken() != null) {
            setRefreshCookie(response, auth.getRefreshToken());
            auth = AuthenticationResponse.builder()
                    .accessToken(auth.getAccessToken())
                    .username(auth.getUsername())
                    .role(auth.getRole())
                    .message(auth.getMessage())
                    .twoFactorRequired(false)
                    .build();
        }
        return ResponseEntity.ok(auth);
    }



    @PostMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verifyOtp(
            @RequestBody VerifyOtpRequest req,
            HttpServletResponse response
    ) {
        String username = jwtService.extractUsernameFromPreAuth(req.getPreAuthToken());
        if (!otpService.verifyOtp(username, req.getOtpCode(), "LOGIN_2FA")) {
            throw new BadCredentialsException("Invalid OTP");
        }
        User user = userRepository.findByUsername(username).orElseThrow();

        AuthenticationResponse auth = authenticationService.completeAuthentication(user);
        if (auth.getRefreshToken() != null) {
            setRefreshCookie(response, auth.getRefreshToken());
            auth = AuthenticationResponse.builder()
                    .accessToken(auth.getAccessToken())
                    .username(auth.getUsername())
                    .role(auth.getRole())
                    .twoFactorRequired(false)
                    .message(auth.getMessage())
                    .build();
        }
        return ResponseEntity.ok(auth);
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) { return token; }
}
