package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log entry for tracking all project lifecycle events.
 * Records who did what, when, and what changed.
 */
@Entity
@Table(name = "project_audit_log", indexes = {
    @Index(name = "idx_audit_project", columnList = "project_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_admin", columnList = "admin_id")
})
public class ProjectAuditLog {
    
    public enum ActionType {
        PROJECT_CREATED("Project Created"),
        PROJECT_UPDATED("Project Updated"),
        PROJECT_VIEWED("Project Viewed"),
        CONTRACTOR_ASSIGNED("Contractor Assigned"),
        CONTRACTOR_UNASSIGNED("Contractor Unassigned"),
        STATUS_CHANGED("Status Changed"),
        PAYMENT_STATUS_CHANGED("Payment Status Changed"),
        PROJECT_DELETED("Project Deleted"),
        CHAT_MESSAGE_SENT("Chat Message Sent"),
        PHOTOS_VIEWED("Photos Viewed"),
        PHOTOS_DOWNLOADED("Photos Downloaded"),
        CONTRACTOR_UPDATED("Contractor Updated"),
        CONTRACTOR_PASSWORD_CHANGED("Contractor Password Changed");
        
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    private Project project;
    
    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;
    
    @Column(name = "old_value", length = 500)
    private String oldValue;
    
    @Column(name = "new_value", length = 500)
    private String newValue;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    protected ProjectAuditLog() {}
    
    public ProjectAuditLog(Project project, ActionType actionType, User admin, 
                          String oldValue, String newValue, String details) {
        this.project = project;
        this.actionType = actionType;
        this.admin = admin;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public ActionType getActionType() { return actionType; }
    public User getAdmin() { return admin; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getIpAddress() { return ipAddress; }
    
    // Setters
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
