package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_chat_read_log",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "admin_username"}))
public class AdminChatReadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "admin_username", nullable = false)
    private String adminUsername;

    @Column(name = "last_read_at", nullable = false)
    private LocalDateTime lastReadAt;

    protected AdminChatReadLog() {}

    public AdminChatReadLog(Project project, String adminUsername) {
        this.project = project;
        this.adminUsername = adminUsername;
        this.lastReadAt = LocalDateTime.now();
    }

    public void markRead() {
        this.lastReadAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getAdminUsername() { return adminUsername; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }
}
