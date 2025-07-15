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
import rs.ac.bg.fon.ebanking.dao.ClientRepository;
import rs.ac.bg.fon.ebanking.dao.EmployeeRepository;
import rs.ac.bg.fon.ebanking.dao.UserRepository;
import rs.ac.bg.fon.ebanking.dto.ClientDTO;
import rs.ac.bg.fon.ebanking.dto.EmployeeDTO;
import rs.ac.bg.fon.ebanking.entity.Client;
import rs.ac.bg.fon.ebanking.entity.Employee;
import rs.ac.bg.fon.ebanking.entity.User;
import rs.ac.bg.fon.ebanking.security.config.JwtService;
import rs.ac.bg.fon.ebanking.security.token.Token;
import rs.ac.bg.fon.ebanking.security.token.TokenRepository;
import rs.ac.bg.fon.ebanking.security.token.TokenType;
import rs.ac.bg.fon.ebanking.entity.Role;
import rs.ac.bg.fon.ebanking.service.implementation.ClientImpl;

import java.io.IOException;

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

    private final ClientImpl clientImpl;


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()->new BadCredentialsException("Data is not valid."));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllMemberTokens(user);
        saveMemberToken(user,jwtToken);

        Integer id;
        String role;

        if(!user.getUsername().contains("employee")){

            Client client = clientRepository.findClientByUserClientUsername(user.getUsername());
            id = client.getId();
            role=Role.ROLE_CLIENT.name();

        }
        else{
            Employee employee = employeeRepository.findEmployeeByUserEmployeeUsername(user.getUsername());
            id = employee.getId();
            role = Role.ROLE_EMPLOYEE.name();
        }

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .id(id)
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



//    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        System.out.println("U refres tokenu sam.");
//        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        final String refreshToken;
//        final String userUsername;
//        if(authHeader==null || !authHeader.startsWith("Bearer ")){
//            return;
//        }
//        refreshToken = authHeader.substring(7);
//        userUsername = jwtService.extractUsername(refreshToken);
//        if(userUsername!=null){
//            var user = this.userRepository.findByUsername(userUsername)
//                    .orElseThrow();
//            if(jwtService.isTokenValid(refreshToken,user)){
//                var accessToken = jwtService.generateToken(user);
//                revokeAllMemberTokens(user);
//                saveMemberToken(user,accessToken);
//                var authResponse = AuthenticationResponse.builder()
//                        .accessToken(accessToken)
//                        .refreshToken(refreshToken)
//                        .build();
//                new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
//            }
//        }
//    }

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

        // If username is present and the token is valid
        if (userUsername != null) {
            var user = userRepository.findByUsername(userUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Ensure the refresh token itself is valid
            if (jwtService.isTokenValid(refreshToken, user)) {
                // Generate new access token
                var newAccessToken = jwtService.generateToken(user);

                // Invalidate all existing tokens and save the new one
                revokeAllMemberTokens(user);
                saveMemberToken(user, newAccessToken);

                // Construct authentication response
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken) // Same refresh token is returned
                        .build();

                // Write the response to output stream
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            } else {
                // If the refresh token is invalid, return unauthorized status
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired refresh token");
            }
        } else {
            // If username extraction fails, return unauthorized status
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
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
