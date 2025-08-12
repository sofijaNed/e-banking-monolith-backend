package rs.ac.bg.fon.ebanking.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {
    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    public void record(String tableName, Long recordId, String action, Object before, Object after) {
        Audit a = new Audit();
        a.setTableName(tableName);
        a.setRecordId(recordId);
        a.setAction(action);
        a.setOldValues(before != null ? serializeObject(before) : null);
        a.setNewValues(after != null ? serializeObject(after) : null);
        a.setChangedAt(Instant.now());
        a.setChangedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        auditRepository.save(a);
    }

    private String serializeObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object for audit log", e);
        }
    }
}

