package rs.ac.bg.fon.ebanking.security.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.ratelimit")
@Data
public class RateLimitProperties {

    /** Globalni prekidač (test/prod). */
    private boolean enabled = true;

    /** Preflight OPTIONS se ne limitira. */
    private boolean skipOptions = true;

    /** login: kapacitet i prozor (npr. 5 req/min po IP/username) */
    private int loginCapacity = 5;
    private Duration loginPeriod = Duration.ofMinutes(1);

    /** refresh: 10 req/min po user-u (ili IP ako nema principal-a) */
    private int refreshCapacity = 10;
    private Duration refreshPeriod = Duration.ofMinutes(1);

    /** transakcije (POST): 3 req/min po user-u */
    private int transactionCapacity = 3;
    private Duration transactionPeriod = Duration.ofMinutes(1);
}
