package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_view_logs")
public class ProjectViewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;
    
    @Column(name = "first_viewed_at", nullable = false)
    private LocalDateTime firstViewedAt;
    
    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 1;
    
    protected ProjectViewLog() {}
    
    public ProjectViewLog(Project project, Contractor contractor) {
        this.project = project;
        this.contractor = contractor;
        this.firstViewedAt = LocalDateTime.now();
        this.lastViewedAt = LocalDateTime.now();
        this.viewCount = 1;
    }
    
    public void recordView() {
        this.lastViewedAt = LocalDateTime.now();
        this.viewCount++;
    }
    
    // Getters
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public Contractor getContractor() { return contractor; }
    public LocalDateTime getFirstViewedAt() { return firstViewedAt; }
    public LocalDateTime getLastViewedAt() { return lastViewedAt; }
    public Integer getViewCount() { return viewCount; }
}
