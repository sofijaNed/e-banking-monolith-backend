package rs.ac.bg.fon.ebanking.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import rs.ac.bg.fon.ebanking.client.ClientRepository;
import rs.ac.bg.fon.ebanking.employee.EmployeeRepository;
import rs.ac.bg.fon.ebanking.user.UserRepository;
import rs.ac.bg.fon.ebanking.client.Client;
import rs.ac.bg.fon.ebanking.employee.Employee;
import rs.ac.bg.fon.ebanking.user.User;
import rs.ac.bg.fon.ebanking.security.auth.AuthenticationRequest;
import rs.ac.bg.fon.ebanking.security.auth.AuthenticationResponse;
import rs.ac.bg.fon.ebanking.security.auth.AuthenticationService;
import rs.ac.bg.fon.ebanking.security.config.JwtService;
import rs.ac.bg.fon.ebanking.security.token.Token;
import rs.ac.bg.fon.ebanking.security.token.TokenRepository;
import rs.ac.bg.fon.ebanking.client.ClientImpl;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Importuj klase User, Token, TokenRepository, JwtService, Client, Employee... iz tvog projekta
// Primer:
// import rs.ac.bg.fon.ebanking.user.User;
// import rs.ac.bg.fon.ebanking.security.token.TokenRepository;
// import rs.ac.bg.fon.ebanking.security.token.Token;
// import rs.ac.bg.fon.ebanking.security.config.JwtService;
// import rs.ac.bg.fon.ebanking.client.ClientRepository;
// import rs.ac.bg.fon.ebanking.employee.EmployeeRepository;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtService jwtService;
    @Mock
    TokenRepository tokenRepository;
    @Mock
    ClientRepository clientRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    ClientImpl clientImpl;
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    AuthenticationService authenticationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    @Test
    void authenticate_AsClient_success() throws Exception {
        // Arrange
        String username = "pera";
        String rawPass = "pwd";

        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername(username);
        request.setPassword(rawPass);

        User user = new User();
        user.setUsername(username);

        Client client = new Client();
        client.setId(42L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        // authenticationManager.authenticate ne baca izuzetak => authentication prolazi
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("user", "pass"));

        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(tokenRepository.findAllValidTokenByUser(username)).thenReturn(java.util.Collections.emptyList());
        when(clientRepository.findClientByUserClientUsername(username)).thenReturn(client);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("client1", response.getUsername());
        assertEquals("ROLE_CLIENT", response.getRole());
        verify(tokenRepository, atLeastOnce()).save(any()); // saveMemberToken pozvan
    }

    @Test
    void authenticate_AsEmployee_success() throws Exception {
        // Arrange
        String username = "employee_john"; // sadrzi "employee" -> employee path
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername(username);
        request.setPassword("pwd");

        User user = new User();
        user.setUsername(username);

        Employee emp = new Employee();
        emp.setId(7L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("user", "pass"));

        when(jwtService.generateToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");
        when(tokenRepository.findAllValidTokenByUser(username)).thenReturn(java.util.Collections.emptyList());
        when(employeeRepository.findEmployeeByUserEmployeeUsername(username)).thenReturn(emp);

        // Act
        AuthenticationResponse resp = authenticationService.authenticate(request);

        // Assert
        assertNotNull(resp);
        assertEquals("access", resp.getAccessToken());
        assertEquals("refresh", resp.getRefreshToken());
        assertEquals("employee1", resp.getUsername());
        assertEquals("ROLE_EMPLOYEE", resp.getRole());
    }

    @Test
    void refreshToken_success() throws IOException {
        // Arrange
        String refreshToken = "refresh-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String username = "pera";
        User user = new User();
        user.setUsername(username);

        Token stored = Token.builder()
                .token(refreshToken)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();

        when(jwtService.extractUsername(refreshToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(tokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(stored));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);

        when(jwtService.generateToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");

        // Act
        authenticationService.refreshToken(request, response);

        // Assert
        assertEquals(200, response.getStatus());
        String content = response.getContentAsString();
        assertNotNull(content);
        JsonNode node = objectMapper.readTree(content);
        assertEquals("new-access", node.get("accessToken").asText());
        assertEquals("new-refresh", node.get("refreshToken").asText());

        // stored token should be revoked and saved
        assertTrue(stored.isRevoked());
        assertTrue(stored.isExpired());
        verify(tokenRepository, atLeast(1)).save(any());
    }

    @Test
    void refreshToken_missingHeader_unauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationService.refreshToken(request, response);

        assertEquals(401, response.getStatus());
    }

    @Test
    void refreshToken_revokedToken_unauthorized() throws Exception {
        String refreshToken = "r";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername(refreshToken)).thenReturn("pera");
        when(userRepository.findByUsername("pera")).thenReturn(Optional.of(new User()));

        Token stored = Token.builder()
                .token(refreshToken)
                .revoked(true)
                .expired(false)
                .build();

        when(tokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(stored));

        authenticationService.refreshToken(request, response);
        assertEquals(401, response.getStatus());
    }
}
public class AuthServicetest {
}
