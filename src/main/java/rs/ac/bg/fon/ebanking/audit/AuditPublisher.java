package rs.ac.bg.fon.ebanking.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class AuditPublisher {
    private final ApplicationEventPublisher publisher;

    @Value("${spring.application.name:monolith-app}")
    private String appName;

    public void success(String action, String principal,
                        String resourceType, String resourceId,
                        Integer httpStatus, Integer durationMs,
                        String checksJson, String detailsJson) {
        publish("SUCCESS", action, principal, resourceType, resourceId, httpStatus, durationMs, checksJson, detailsJson);
    }

    public void fail(String action, String principal,
                     String resourceType, String resourceId,
                     Integer httpStatus, Integer durationMs,
                     String checksJson, String detailsJson) {
        publish("FAIL", action, principal, resourceType, resourceId, httpStatus, durationMs, checksJson, detailsJson);
    }

    private void publish(String outcome, String action, String principal,
                         String resourceType, String resourceId,
                         Integer httpStatus, Integer durationMs,
                         String checksJson, String detailsJson) {

        AuditEventPayload p = new AuditEventPayload();
        p.setService(appName);
        p.setAction(action);
        p.setPrincipal(principal);
        p.setOutcome(outcome);
        p.setResourceType(resourceType);
        p.setResourceId(resourceId);
        p.setHttpStatus(httpStatus);
        p.setDurationMs(durationMs);
        p.setChecksJson(checksJson);
        p.setDetailsJson(detailsJson);

        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();
            p.setIp(first(req.getHeader("X-Forwarded-For"), req.getRemoteAddr()));
            p.setUserAgent(req.getHeader("User-Agent"));
            p.setHttpMethod(req.getMethod());
            p.setHttpPath(req.getRequestURI());
            p.setCorrelationId(MDC.get("cid"));
            if (p.getCorrelationId() == null || p.getCorrelationId().isBlank()) {
                p.setCorrelationId(first(req.getHeader("X-Correlation-ID"), null));
            }
        }

        publisher.publishEvent(p);
    }

    private String first(String... v) {
        for (String s : v) if (s != null && !s.isBlank()) return s;
        return null;
    }
}
