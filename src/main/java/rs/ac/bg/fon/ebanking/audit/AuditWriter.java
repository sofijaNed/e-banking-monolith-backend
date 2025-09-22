package rs.ac.bg.fon.ebanking.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditWriter {
    private final AuditRepository repo;

    @Async("auditExecutor")
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void writeAsync(Audit a) {
        if (a.getTs() == null) a.setTs(LocalDateTime.now());
        repo.save(a);
    }
}
