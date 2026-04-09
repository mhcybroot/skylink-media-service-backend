package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_audit_log", indexes = {
    @Index(name = "idx_sys_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_sys_audit_actor", columnList = "actor_id"),
    @Index(name = "idx_sys_audit_target", columnList = "target_id")
})
public class SystemAuditLog {

    public enum ActionType {
        USER_CREATED("User Created"),
        USER_UPDATED("User Updated"),
        USER_DELETED("User Deleted"),
        USER_BLOCKED("User Blocked"),
        USER_UNBLOCKED("User Unblocked"),
        ADMIN_CREATED("Admin Created"),
        ADMIN_DELETED("Admin Deleted"),
        SUPER_ADMIN_CREATED("Super Admin Created"),
        LOGIN_SUCCESS("Login Success"),
        LOGIN_FAILURE("Login Failure"),
        LOGOUT("Logout"),
        SYSTEM_CONFIG_CHANGED("System Config Changed");

        private final String displayName;

        ActionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_username")
    private String actorUsername;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    protected SystemAuditLog() {}

    public SystemAuditLog(ActionType actionType, Long actorId, String targetType, Long targetId, String details) {
        this.actionType = actionType;
        this.actorId = actorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public ActionType getActionType() { return actionType; }
    public Long getActorId() { return actorId; }
    public String getActorUsername() { return actorUsername; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getIpAddress() { return ipAddress; }

    // Setters
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
