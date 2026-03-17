package root.cyb.mh.skylink_media_service.application.dto.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDetailResponse {
    private Long id;
    private String workOrderNumber;
    private String location;
    private String clientCode;
    private String description;
    private String status;
    private String paymentStatus;
    private LocalDate dueDate;
    private LocalDate receivedDate;
    private String workType;
    private String address;
    private String customer;
    private List<PhotoInfo> photos;
    private LocalDateTime assignedAt;

    public ProjectDetailResponse() {}

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
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    
    public List<PhotoInfo> getPhotos() { return photos; }
    public void setPhotos(List<PhotoInfo> photos) { this.photos = photos; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public static class PhotoInfo {
        private Long id;
        private String fileName;
        private String thumbnailUrl;
        private LocalDateTime uploadedAt;

        public PhotoInfo() {}

        public PhotoInfo(Long id, String fileName, String thumbnailUrl, LocalDateTime uploadedAt) {
            this.id = id;
            this.fileName = fileName;
            this.thumbnailUrl = thumbnailUrl;
            this.uploadedAt = uploadedAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }
}
