package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public String generateCsv(List<Project> projects) {
        StringBuilder csv = new StringBuilder();
        
        // UTF-8 BOM for Excel compatibility
        csv.append('\uFEFF');
        
        // Headers
        csv.append("Work Order,Client Code,Location,Status,Payment Status,Description,")
           .append("PPW Number,Work Type,Client Company,Customer,Loan Number,")
           .append("Received Date,Due Date,Assigned To,WO Admin,Invoice Price,")
           .append("Assigned Contractors,Photo Count,Created At\n");
        
        // Data rows
        for (Project project : projects) {
            csv.append(escapeCsv(project.getWorkOrderNumber())).append(",");
            csv.append(escapeCsv(project.getClientCode())).append(",");
            csv.append(escapeCsv(project.getLocation())).append(",");
            csv.append(escapeCsv(project.getStatus() != null ? project.getStatus().getDisplayName() : "")).append(",");
            csv.append(escapeCsv(project.getPaymentStatus() != null ? project.getPaymentStatus().getDisplayName() : "")).append(",");
            csv.append(escapeCsv(project.getDescription())).append(",");
            csv.append(escapeCsv(project.getPpwNumber())).append(",");
            csv.append(escapeCsv(project.getWorkType())).append(",");
            csv.append(escapeCsv(project.getClientCompany())).append(",");
            csv.append(escapeCsv(project.getCustomer())).append(",");
            csv.append(escapeCsv(project.getLoanNumber())).append(",");
            csv.append(escapeCsv(project.getReceivedDate() != null ? project.getReceivedDate().format(DATE_FORMATTER) : "")).append(",");
            csv.append(escapeCsv(project.getDueDate() != null ? project.getDueDate().format(DATE_FORMATTER) : "")).append(",");
            csv.append(escapeCsv(project.getAssignedTo())).append(",");
            csv.append(escapeCsv(project.getWoAdmin())).append(",");
            csv.append(escapeCsv(project.getInvoicePrice() != null ? project.getInvoicePrice().toString() : "")).append(",");
            csv.append(escapeCsv(getAssignedContractors(project))).append(",");
            csv.append(project.getPhotos() != null ? project.getPhotos().size() : 0).append(",");
            csv.append(escapeCsv(project.getCreatedAt() != null ? project.getCreatedAt().format(DATETIME_FORMATTER) : "")).append("\n");
        }
        
        return csv.toString();
    }
    
    private String getAssignedContractors(Project project) {
        if (project.getAssignments() == null || project.getAssignments().isEmpty()) {
            return "";
        }
        return project.getAssignments().stream()
                .map(ProjectAssignment::getContractor)
                .map(contractor -> contractor.getFullName() != null ? contractor.getFullName() : contractor.getUsername())
                .collect(Collectors.joining("; "));
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // Check if escaping is needed
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r") ||
            value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            
            // Escape double quotes by doubling them
            String escaped = value.replace("\"", "\"\"");
            
            // Prevent CSV injection by prefixing formulas with single quote
            if (escaped.startsWith("=") || escaped.startsWith("+") || escaped.startsWith("-") || escaped.startsWith("@")) {
                escaped = "'" + escaped;
            }
            
            // Wrap in double quotes
            return "\"" + escaped + "\"";
        }
        
        return value;
    }
}
