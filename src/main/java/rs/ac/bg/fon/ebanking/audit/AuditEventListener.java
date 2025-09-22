package rs.ac.bg.fon.ebanking.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditEventListener {
    private final AuditWriter writer;

    @EventListener
    public void onAudit(AuditEventPayload p) {
        Audit a = new Audit();
        a.setTs(LocalDateTime.now());
        a.setService(p.getService());
        a.setAction(p.getAction());
        a.setPrincipal(p.getPrincipal());
        a.setOutcome(p.getOutcome());
        a.setIp(p.getIp());
        a.setUserAgent(p.getUserAgent());
        a.setResourceType(p.getResourceType());
        a.setResourceId(p.getResourceId());
        a.setCorrelationId(p.getCorrelationId());
        a.setHttpMethod(p.getHttpMethod());
        a.setHttpPath(p.getHttpPath());
        a.setHttpStatus(p.getHttpStatus());
        a.setDurationMs(p.getDurationMs());
        a.setChecksJson(p.getChecksJson());
        a.setDetailsJson(p.getDetailsJson());
        a.setTagsJson(p.getTagsJson());

        writer.writeAsync(a);
    }
}
