package rs.ac.bg.fon.ebanking.security.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.ratelimit")
@Data
public class RateLimitProperties {

    private boolean enabled = true;

    private boolean skipOptions = true;

    private int loginCapacity = 5;
    private Duration loginPeriod = Duration.ofMinutes(1);

    private int refreshCapacity = 10;
    private Duration refreshPeriod = Duration.ofMinutes(1);

    private int transactionCapacity = 3;
    private Duration transactionPeriod = Duration.ofMinutes(1);
}
