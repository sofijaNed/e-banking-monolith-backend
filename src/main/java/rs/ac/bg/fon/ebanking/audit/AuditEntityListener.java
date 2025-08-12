package rs.ac.bg.fon.ebanking.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;

public class AuditEntityListener {

    @Setter
    private static AuditRepository auditRepository;
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static AuditService auditService;


    @PostPersist
    public void afterCreate(Object entity) {
        Long recordId = extractId(entity);
        auditService.record(
                entity.getClass().getSimpleName(),
                recordId,
                "INSERT",
                null,
                entity
        );
    }

    @PostUpdate
    public void afterUpdate(Object entity) {
        Long recordId = extractId(entity);
        // Napomena: za before/after treba hvatati stari state iz @PreUpdate
        auditService.record(
                entity.getClass().getSimpleName(),
                recordId,
                "UPDATE",
                null, // ovde možeš kasnije dodati pre-update stanje
                entity
        );
    }

    @PostRemove
    public void afterDelete(Object entity) {
        Long recordId = extractId(entity);
        auditService.record(
                entity.getClass().getSimpleName(),
                recordId,
                "DELETE",
                entity,
                null
        );
    }

    private Long extractId(Object entity) {
        try {
            return (Long) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            throw new RuntimeException("Could not extract ID from entity", e);
        }
    }

    private void createAuditRecord(Object entity, String action, Object oldEntity) {
        if (auditRepository == null) {
            return;
        }

        Audit audit = new Audit();
        audit.setTableName(entity.getClass().getSimpleName());
        audit.setRecordId(getEntityId(entity));
        audit.setAction(action);
        audit.setChangedAt(Instant.now());
        audit.setChangedBy(getCurrentUsername());

        try {
            String newJson = objectMapper.writeValueAsString(entity);
            audit.setNewValues(newJson);
            if (oldEntity != null) {
                audit.setOldValues(objectMapper.writeValueAsString(oldEntity));
            }
        } catch (JsonProcessingException e) {
            audit.setNewValues("{}");
            audit.setOldValues("{}");
        }

        auditRepository.save(audit);
    }

    private Long getEntityId(Object entity) {
        try {
            return (Long) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "SYSTEM";
    }
}
