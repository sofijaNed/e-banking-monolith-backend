package rs.ac.bg.fon.ebanking.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable {
    @CreatedDate
    @Column(name="created_at", updatable = false)
    protected Instant createdAt;

    @LastModifiedDate
    @Column(name="updated_at")
    protected Instant updatedAt;

    @CreatedBy
    @Column(name="created_by", updatable = false)
    protected String createdBy;

    @LastModifiedBy
    @Column(name="updated_by")
    protected String updatedBy;
}
