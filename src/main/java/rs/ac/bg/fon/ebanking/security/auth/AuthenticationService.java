package rs.ac.bg.fon.ebanking.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.audit.Audit;
import rs.ac.bg.fon.ebanking.audit.AuditRepository;
import rs.ac.bg.fon.ebanking.client.ClientRepository;
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

import java.io.IOException;
import java.time.Instant;

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
    private final AuditRepository auditRepository;


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()->new BadCredentialsException("Data is not valid."));

        if (user.getTwoFactorEnabled() && Boolean.TRUE.equals(request.isUse2fa())) {
            String preAuthToken = jwtService.generatePreAuthToken(user);

            otpService.generateAndSendOtp(user, request.getEmail(), "LOGIN_2FA");

            return AuthenticationResponse.builder()
                    .twoFactorRequired(true)
                    .preAuthToken(preAuthToken)
                    .message("OTP poslat na email")
                    .build();
        }
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllMemberTokens(user);
        saveMemberToken(user,jwtToken);

        Long id;
        String role;
        String name = "";

        if(user.getRole() == Role.ROLE_CLIENT){

            Client client = clientRepository.findClientByUserClientUsername(user.getUsername());
            id = client.getId();
            role=Role.ROLE_CLIENT.name();
            name = client.getFirstname();

        }
        else {
            Employee employee = employeeRepository.findEmployeeByUserEmployeeUsername(user.getUsername());
            id = employee.getId();
            role = Role.ROLE_EMPLOYEE.name();
            name = employee.getFirstname();
        }

        Audit audit = new Audit();
        audit.setTableName("users");
        audit.setRecordId(id);
        audit.setAction("LOGIN");
        audit.setChangedAt(Instant.now());
        audit.setChangedBy(name);

        auditRepository.save(audit);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(role)
                .message("Succesfull logging.")
                .build();
    }

    public Object checkForActiveUser(String token) throws Exception {
        //String jwt = token.substring(7);

        String username = jwtService.extractUsername(token);
        if(jwtService.isTokenExpired(token)) username = null;

        if(username != null && !username.contains("employee")){

            return clientImpl.findByUsername(username);

        }
        else{
            Employee employee = employeeRepository.findEmployeeByUserEmployeeUsername(username);
            return modelMapper.map(employee, EmployeeDTO.class);
        }

    }

    public AuthenticationResponse completeAuthentication(User user) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllMemberTokens(user);
        saveMemberToken(user, jwtToken);

        Long id;
        String role;
        String name = "";

        if (user.getRole() == Role.ROLE_CLIENT) {
            Client client = clientRepository.findClientByUserClientUsername(user.getUsername());
            id = client.getId();
            role = Role.ROLE_CLIENT.name();
            name = client.getFirstname();
        } else {
            Employee employee = employeeRepository.findEmployeeByUserEmployeeUsername(user.getUsername());
            id = employee.getId();
            role = Role.ROLE_EMPLOYEE.name();
            name = employee.getFirstname();
        }

        Audit audit = new Audit();
        audit.setTableName("users");
        audit.setRecordId(id);
        audit.setAction("LOGIN");
        audit.setChangedAt(Instant.now());
        audit.setChangedBy(name);
        auditRepository.save(audit);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .twoFactorRequired(false)
                .role(user.getRole().name())
                .message("Successfully verified OTP")
                .build();
    }


    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("In refresh token method.");

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userUsername;

        // Check for Bearer token in the Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // No token present
        }

        refreshToken = authHeader.substring(7); // Extract refresh token
        userUsername = jwtService.extractUsername(refreshToken);

        if (userUsername == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // If username is present and the token is valid
        var user = userRepository.findByUsername(userUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var storedRefreshOpt = tokenRepository.findByToken(refreshToken);
        if (storedRefreshOpt.isEmpty() || storedRefreshOpt.get().isRevoked() || storedRefreshOpt.get().isExpired()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 2) Ensure signature and expiry valid
        if (!jwtService.isTokenValid(refreshToken, user)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        var storedRefresh = storedRefreshOpt.get();
        storedRefresh.setRevoked(true);
        storedRefresh.setExpired(true);
        tokenRepository.save(storedRefresh);

        // 4) Create new access and new refresh token
        var newAccessToken = jwtService.generateToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);

        saveMemberToken(user, newAccessToken); // access token saved as before
        // save new refresh token with TokenType.REFRESH (or the same enum if extended)
        var refreshTokenEntity = Token.builder()
                .token(newRefreshToken)
                .user(user)
                .tokenType(TokenType.BEARER) // ideally REFRESH if you add it
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(refreshTokenEntity);

        var authResponse = AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
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
}
