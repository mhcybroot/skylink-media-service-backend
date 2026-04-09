package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_audit_log", indexes = {
    @Index(name = "idx_login_user", columnList = "user_id"),
    @Index(name = "idx_login_timestamp", columnList = "login_time"),
    @Index(name = "idx_login_successful", columnList = "successful")
})
public class LoginAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "user_type", length = 20)
    private String userType;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "successful", nullable = false)
    private Boolean successful;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    protected LoginAuditLog() {}

    public LoginAuditLog(String username, String userType, Boolean successful) {
        this.username = username;
        this.userType = userType;
        this.successful = successful;
        this.loginTime = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getUserType() { return userType; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public LocalDateTime getLogoutTime() { return logoutTime; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public Boolean getSuccessful() { return successful; }
    public String getFailureReason() { return failureReason; }
    public String getSessionId() { return sessionId; }

    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
