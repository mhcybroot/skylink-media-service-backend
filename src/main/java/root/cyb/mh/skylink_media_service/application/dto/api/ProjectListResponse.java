package root.cyb.mh.skylink_media_service.application.dto.api;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectListResponse {
    private Long id;
    private String workOrderNumber;
    private String location;
    private String clientCode;
    private String description;
    private String status;
    private LocalDate dueDate;
    private int photoCount;
    private LocalDateTime assignedAt;

    public ProjectListResponse() {}

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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public int getPhotoCount() { return photoCount; }
    public void setPhotoCount(int photoCount) { this.photoCount = photoCount; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
