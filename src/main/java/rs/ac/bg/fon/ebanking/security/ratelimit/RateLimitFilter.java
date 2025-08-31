package rs.ac.bg.fon.ebanking.security.ratelimit;

// imports bez Refill-a i bez Bucket4j klase
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;

    // stabilno čuvamo kante po ključu (ruta + identitet)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!props.isEnabled()) return true;
        if (props.isSkipOptions() && "OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String path = request.getRequestURI();
        String method = request.getMethod();

        // limitiramo samo rute koje nas zanimaju
        if (HttpMethod.POST.matches(method) && path.startsWith("/auth/authenticate")) return false;
        if (HttpMethod.POST.matches(method) && path.startsWith("/auth/refresh")) return false;
        if (HttpMethod.POST.matches(method) && path.startsWith("/transactions")) return false;

        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        String method = req.getMethod();

        String subject = (req.getUserPrincipal() != null) ? req.getUserPrincipal().getName() : null;
        String ip = req.getRemoteAddr();

        String bucketKey;
        int capacity;
        Duration period;

        if (HttpMethod.POST.matches(method) && path.startsWith("/auth/authenticate")) {
            bucketKey = "login:" + (subject != null ? subject : ip);
            capacity = props.getLoginCapacity();
            period = props.getLoginPeriod();
        } else if (HttpMethod.POST.matches(method) && path.startsWith("/auth/refresh")) {
            bucketKey = "refresh:" + (subject != null ? subject : ip);
            capacity = props.getRefreshCapacity();
            period = props.getRefreshPeriod();
        } else if (HttpMethod.POST.matches(method) && path.startsWith("/transactions")) {
            bucketKey = "tx:" + (subject != null ? subject : ip);
            capacity = props.getTransactionCapacity();
            period = props.getTransactionPeriod();
        } else {
            chain.doFilter(req, res);
            return;
        }

        System.out.println("[RL] method=" + method + " path=" + path +
                " key=" + bucketKey + " cap=" + capacity + " period=" + period);

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k ->
                Bucket.builder()
                        .addLimit(limit -> limit
                                .capacity(capacity)
                                .refillGreedy(capacity, period)
                        )
                        .build()
        );

        var probe = bucket.tryConsumeAndReturnRemaining(1);

        System.out.println("[RL] consumed=" + probe.isConsumed() +
                " remaining=" + probe.getRemainingTokens());
        if (probe.isConsumed()) {
            res.setHeader("RateLimit-Limit", String.valueOf(capacity));
            res.setHeader("RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(req, res);
        } else {
            long seconds = Math.max(1, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());
            res.setHeader("Retry-After", String.valueOf(seconds));
            res.setHeader("RateLimit-Reset", String.valueOf(seconds));
            res.setStatus(429);
            res.getWriter().write("Too Many Requests");
        }
    }
}
