package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "work_order_number", unique = true, nullable = false)
    private String workOrderNumber;
    
    @Column(nullable = false)
    private String location;
    
    @Column(name = "client_code", nullable = false)
    private String clientCode;
    
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectAssignment> assignments;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Photo> photos;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Project() {}
    
    public Project(String workOrderNumber, String location, String clientCode, String description) {
        this.workOrderNumber = workOrderNumber;
        this.location = location;
        this.clientCode = clientCode;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getClientCode() { return clientCode; }
    public void setClientCode(String clientCode) { this.clientCode = clientCode; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<ProjectAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<ProjectAssignment> assignments) { this.assignments = assignments; }
    
    public List<Photo> getPhotos() { return photos; }
    public void setPhotos(List<Photo> photos) { this.photos = photos; }
}
