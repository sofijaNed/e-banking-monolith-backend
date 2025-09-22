package rs.ac.bg.fon.ebanking.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Value("${app.correlation.header:X-Correlation-ID}")
    private String headerName;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String incoming = Optional.ofNullable(req.getHeader(headerName))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        MDC.put("cid", incoming);
        try {
            res.setHeader(headerName, incoming);
            chain.doFilter(req, res);
        } finally {
            MDC.remove("cid");
        }
    }
}
