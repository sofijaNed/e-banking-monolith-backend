package rs.ac.bg.fon.ebanking.audit;

import lombok.Data;

@Data
public class AuditEventPayload {
    private String service;
    private String action;
    private String principal;
    private String outcome;
    private String ip;
    private String userAgent;
    private String resourceType;
    private String resourceId;
    private String correlationId;
    private String httpMethod;
    private String httpPath;
    private Integer httpStatus;
    private Integer durationMs;
    private String checksJson;
    private String detailsJson;
    private String tagsJson;
}
