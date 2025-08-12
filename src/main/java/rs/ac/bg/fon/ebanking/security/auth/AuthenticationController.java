package rs.ac.bg.fon.ebanking.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.bg.fon.ebanking.dao.UserRepository;
import rs.ac.bg.fon.ebanking.entity.User;
import rs.ac.bg.fon.ebanking.security.config.JwtService;
import rs.ac.bg.fon.ebanking.security.twofactorauth.OtpService;
import rs.ac.bg.fon.ebanking.security.twofactorauth.VerifyOtpRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final UserRepository userRepository;

    @GetMapping("/me/{token}")
    public ResponseEntity<AuthenticationResponse> getCurrentUser(@PathVariable("token") String token) throws Exception {

        String username = jwtService.extractUsername(token);
        if (username == null || jwtService.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .username(user.get().getUsername())
                    .role(user.get().getRole().name())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authentication(@Valid @RequestBody AuthenticationRequest request){
        System.out.println("Prvo");
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }



    @PostMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @PostMapping("/verify-otp")
    public AuthenticationResponse verifyOtp(@RequestBody VerifyOtpRequest req) {
        String username = jwtService.extractUsernameFromPreAuth(req.getPreAuthToken());
        if (!otpService.verifyOtp(username, req.getOtpCode(), "LOGIN_2FA")) {
            throw new BadCredentialsException("Invalid OTP");
        }
        User user = userRepository.findByUsername(username).orElseThrow();
        return authenticationService.completeAuthentication(user);
    }
}
