package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "webp_path")
    private String webpPath;
    
    @Column(name = "thumbnail_path")
    private String thumbnailPath;
    
    @Column(name = "original_path")
    private String originalPath;
    
    @Column(name = "is_optimized")
    private Boolean isOptimized = false;
    
    @Column(name = "optimized_at")
    private LocalDateTime optimizedAt;
    
    @Column(name = "optimization_status")
    private String optimizationStatus = "PENDING";
    
    @Column(name = "original_name")
    private String originalName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Photo() {}
    
    public Photo(String fileName, String filePath, String originalName, Long fileSize, Project project, Contractor contractor) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.project = project;
        this.contractor = contractor;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public Contractor getContractor() { return contractor; }
    public void setContractor(Contractor contractor) { this.contractor = contractor; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public String getWebpPath() { return webpPath; }
    public void setWebpPath(String webpPath) { this.webpPath = webpPath; }
    
    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
    
    public String getOriginalPath() { return originalPath; }
    public void setOriginalPath(String originalPath) { this.originalPath = originalPath; }
    
    public Boolean getIsOptimized() { return isOptimized; }
    public void setIsOptimized(Boolean isOptimized) { this.isOptimized = isOptimized; }
    
    public LocalDateTime getOptimizedAt() { return optimizedAt; }
    public void setOptimizedAt(LocalDateTime optimizedAt) { this.optimizedAt = optimizedAt; }
    
    public String getOptimizationStatus() { return optimizationStatus; }
    public void setOptimizationStatus(String optimizationStatus) { this.optimizationStatus = optimizationStatus; }
}
