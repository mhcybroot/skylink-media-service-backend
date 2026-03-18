package root.cyb.mh.skylink_media_service.application.dto;

import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.domain.valueobjects.PaymentStatus;
import java.time.LocalDate;
import java.math.BigDecimal;

public class ProjectSearchCriteria {
    private String textSearch;
    private ProjectStatus status;
    private PaymentStatus paymentStatus;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private Long assignedContractorId;
    
    public ProjectSearchCriteria() {}
    
    public boolean isEmpty() {
        return textSearch == null && status == null && paymentStatus == null &&
               dueDateFrom == null && dueDateTo == null && priceFrom == null &&
               priceTo == null && assignedContractorId == null;
    }
    
    public String getTextSearch() { return textSearch; }
    public void setTextSearch(String textSearch) { this.textSearch = textSearch; }
    
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public LocalDate getDueDateFrom() { return dueDateFrom; }
    public void setDueDateFrom(LocalDate dueDateFrom) { this.dueDateFrom = dueDateFrom; }
    
    public LocalDate getDueDateTo() { return dueDateTo; }
    public void setDueDateTo(LocalDate dueDateTo) { this.dueDateTo = dueDateTo; }
    
    public BigDecimal getPriceFrom() { return priceFrom; }
    public void setPriceFrom(BigDecimal priceFrom) { this.priceFrom = priceFrom; }
    
    public BigDecimal getPriceTo() { return priceTo; }
    public void setPriceTo(BigDecimal priceTo) { this.priceTo = priceTo; }
    
    public Long getAssignedContractorId() { return assignedContractorId; }
    public void setAssignedContractorId(Long assignedContractorId) { this.assignedContractorId = assignedContractorId; }
}
