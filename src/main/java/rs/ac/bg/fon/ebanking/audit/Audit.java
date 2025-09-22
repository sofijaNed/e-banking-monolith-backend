package rs.ac.bg.fon.ebanking.audit;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_event")
@Data
public class Audit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ts")
    private LocalDateTime ts;

    @Column(name = "service")
    private String service;

    @Column(name = "action")
    private String action;

    @Column(name = "principal")
    private String principal;

    @Column(name = "outcome")
    private String outcome;

    @Column(name = "ip")
    private String ip;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "http_path")
    private String httpPath;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "checks_json", columnDefinition = "json")
    private String checksJson;

    @Column(name = "details_json", columnDefinition = "json")
    private String detailsJson;

    @Column(name = "tags_json", columnDefinition = "json")
    private String tagsJson;
}
