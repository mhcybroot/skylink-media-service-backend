package root.cyb.mh.skylink_media_service.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectDTO {
    private Long id;
    private String workOrderNumber;
    private String location;
    private String clientCode;
    private String description;
    private String status;
    private String paymentStatus;
    private LocalDate receivedDate;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private String statusUpdatedBy;
    private LocalDateTime statusUpdatedAt;

    // Constructors
    public ProjectDTO() {}

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatusUpdatedBy() { return statusUpdatedBy; }
    public void setStatusUpdatedBy(String statusUpdatedBy) { this.statusUpdatedBy = statusUpdatedBy; }

    public LocalDateTime getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(LocalDateTime statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }
}
