package rs.ac.bg.fon.ebanking.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import rs.ac.bg.fon.ebanking.security.filter.JwtAuthenticationFilter;
import rs.ac.bg.fon.ebanking.security.ratelimit.RateLimitFilter;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Value("${jwt.refresh-cookie-name:refresh_token}")
    private String refreshCookieName;
    @Value("${jwt.refresh-cookie-path:/}")
    private String refreshCookiePath;
    @Value("${jwt.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;


    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, RateLimitFilter rateLimitFilter) throws Exception {
        var repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieName("XSRF-TOKEN");
        repo.setHeaderName("X-XSRF-TOKEN");
        repo.setCookiePath("/");
        repo.setSecure(false);
        repo.setCookieCustomizer(c -> c.sameSite("Lax"));

        var requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        System.out.println("Proverava konfiguraciju server");
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(List.of("http://localhost:4200"));
                        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(List.of(
                                "Authorization","Content-Type","X-XSRF-TOKEN","X-CSRF-TOKEN","Idempotency-Key"
                        ));
                        config.setExposedHeaders(List.of("Authorization",
                                "RateLimit-Limit","RateLimit-Remaining","RateLimit-Reset","Retry-After"));
                        config.setMaxAge(3600L);
                        return config;
                    }
                })).csrf(csrf -> csrf
                        .csrfTokenRepository(repo)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(
                                "/auth/authenticate",
                                "/auth/verify-otp"
                        )
                )
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(HttpMethod.POST, "/transactions/savePliz").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/transactions/sender/{id}").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/transactions/receiver/{id}").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/transactions/{id}").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/accounts/{id}").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/accounts/clients/{id}").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/clients").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/clients/byUser/{username}").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/clients/{id}").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/employees/{id}").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/employees/byUser/{username}").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/loans/client/{clientId}/request").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/loans/{loanId}/approve").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/loans/client/{clientId}").hasAnyRole("CLIENT","EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/loans/status/{status}").hasAnyRole("CLIENT","EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/loan-payments/loan/{loanId}").hasAnyRole("CLIENT","EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/loan-payments/{paymentId}/pay").hasRole("CLIENT")
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/auth/**").permitAll())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((req, res, auth) -> {
                            ResponseCookie delete = ResponseCookie.from("refresh_token", "")
                                    .httpOnly(true)
                                    .secure(false)      // DEV=false (isti kao pri setovanju)
                                    .sameSite("Strict")
                                    .path(refreshCookiePath)          // isti path
                                    .maxAge(0)
                                    .build();
                            res.addHeader(HttpHeaders.SET_COOKIE,
                                    ResponseCookie.from("refresh_token", "")
                                            .httpOnly(true)
                                            .secure(false)
                                            .sameSite("Lax")
                                            .path("/")
                                            .maxAge(0)
                                            .build().toString());
                            res.addHeader(HttpHeaders.SET_COOKIE,
                                    ResponseCookie.from("refresh_token", "")
                                            .httpOnly(true)
                                            .secure(false)
                                            .sameSite("Lax")
                                            .path("/auth")
                                            .maxAge(0)
                                            .build().toString());
                            SecurityContextHolder.clearContext();
                            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        })
                );
        return http.build();
    }


}
