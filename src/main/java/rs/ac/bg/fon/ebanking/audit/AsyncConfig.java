package rs.ac.bg.fon.ebanking.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import rs.ac.bg.fon.ebanking.correlation.MdcTaskDecorator;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(1000);
        ex.setThreadNamePrefix("audit-");
        ex.initialize();
        return ex;
    }

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(1000);
        ex.setThreadNamePrefix("mail-");
        ex.setTaskDecorator(new MdcTaskDecorator());
        ex.initialize();
        return ex;
    }
}
