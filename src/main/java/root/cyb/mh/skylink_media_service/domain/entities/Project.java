package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
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
    
    // New optional fields
    @Column(name = "ppw_number")
    private String ppwNumber;
    
    @Column(name = "work_type")
    private String workType;
    
    @Column(name = "work_details")
    private String workDetails;
    
    @Column(name = "client_company")
    private String clientCompany;
    
    private String customer;
    
    @Column(name = "loan_number")
    private String loanNumber;
    
    @Column(name = "loan_type")
    private String loanType;
    
    private String address;
    
    @Column(name = "received_date")
    private LocalDate receivedDate;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "assigned_to")
    private String assignedTo;
    
    @Column(name = "wo_admin")
    private String woAdmin;
    
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
    
    // Enhanced constructor with all fields
    public Project(String workOrderNumber, String location, String clientCode, String description,
                   String ppwNumber, String workType, String workDetails, String clientCompany,
                   String customer, String loanNumber, String loanType, String address,
                   LocalDate receivedDate, LocalDate dueDate, String assignedTo, String woAdmin) {
        this.workOrderNumber = workOrderNumber;
        this.location = location;
        this.clientCode = clientCode;
        this.description = description;
        this.ppwNumber = ppwNumber;
        this.workType = workType;
        this.workDetails = workDetails;
        this.clientCompany = clientCompany;
        this.customer = customer;
        this.loanNumber = loanNumber;
        this.loanType = loanType;
        this.address = address;
        this.receivedDate = receivedDate;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        this.woAdmin = woAdmin;
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
    
    // New field getters and setters
    public String getPpwNumber() { return ppwNumber; }
    public void setPpwNumber(String ppwNumber) { this.ppwNumber = ppwNumber; }
    
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    
    public String getWorkDetails() { return workDetails; }
    public void setWorkDetails(String workDetails) { this.workDetails = workDetails; }
    
    public String getClientCompany() { return clientCompany; }
    public void setClientCompany(String clientCompany) { this.clientCompany = clientCompany; }
    
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    
    public String getLoanNumber() { return loanNumber; }
    public void setLoanNumber(String loanNumber) { this.loanNumber = loanNumber; }
    
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    public String getWoAdmin() { return woAdmin; }
    public void setWoAdmin(String woAdmin) { this.woAdmin = woAdmin; }
}
