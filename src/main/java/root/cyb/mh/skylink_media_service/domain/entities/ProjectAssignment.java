package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_assignments")
public class ProjectAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
    
    // Constructors
    public ProjectAssignment() {}
    
    public ProjectAssignment(Project project, Contractor contractor) {
        this.project = project;
        this.contractor = contractor;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public Contractor getContractor() { return contractor; }
    public void setContractor(Contractor contractor) { this.contractor = contractor; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
