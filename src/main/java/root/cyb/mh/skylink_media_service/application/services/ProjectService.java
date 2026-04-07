package root.cyb.mh.skylink_media_service.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.application.dto.ProjectSearchCriteria;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.domain.entities.Photo;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.PhotoRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectViewLogRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectMessageRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ProjectService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;
    
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private ProjectViewLogRepository projectViewLogRepository;
    
    @Autowired
    private ProjectMessageRepository projectMessageRepository;
    
    @Autowired
    private AuditLogService auditLogService;
    
    public Project createProject(String workOrderNumber, String location, String clientCode, String description) {
        return createProject(workOrderNumber, location, clientCode, description, 
                           null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    public Project createProject(String workOrderNumber, String location, String clientCode, String description,
                               String ppwNumber, String workType, String workDetails, String clientCompany,
                               String customer, String loanNumber, String loanType, String address,
                               LocalDate receivedDate, LocalDate dueDate, String assignedTo, String woAdmin,
                               java.math.BigDecimal invoicePrice) {
        return createProject(workOrderNumber, location, clientCode, description,
                           ppwNumber, workType, workDetails, clientCompany,
                           customer, loanNumber, loanType, address,
                           receivedDate, dueDate, assignedTo, woAdmin, invoicePrice, null);
    }
    
    public Project createProject(String workOrderNumber, String location, String clientCode, String description,
                               String ppwNumber, String workType, String workDetails, String clientCompany,
                               String customer, String loanNumber, String loanType, String address,
                               LocalDate receivedDate, LocalDate dueDate, String assignedTo, String woAdmin,
                               java.math.BigDecimal invoicePrice, User createdBy) {
        if (projectRepository.existsByWorkOrderNumber(workOrderNumber)) {
            throw new RuntimeException("Work order number already exists");
        }
        
        Project project = new Project(workOrderNumber, location, clientCode, description,
                                    ppwNumber, workType, workDetails, clientCompany,
                                    customer, loanNumber, loanType, address,
                                    receivedDate, dueDate, assignedTo, woAdmin, invoicePrice);
        project.setCreatedBy(createdBy);
        project = projectRepository.save(project);
        
        // Log project creation
        if (createdBy != null) {
            auditLogService.logProjectCreated(project, createdBy);
        }
        
        return project;
    }
    
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Project not found"));
    }
    
    public ProjectAssignment assignContractorToProject(Long projectId, Long contractorId) {
        return assignContractorToProject(projectId, contractorId, null);
    }
    
    public ProjectAssignment assignContractorToProject(Long projectId, Long contractorId, User admin) {
        Project project = getProjectById(projectId);
        Contractor contractor = contractorRepository.findById(contractorId)
            .orElseThrow(() -> new RuntimeException("Contractor not found"));
            
        if (projectAssignmentRepository.findByProjectAndContractor(project, contractor).isPresent()) {
            throw new RuntimeException("Contractor already assigned to this project");
        }
    
        // Business Rule 1: One contractor can be assigned to maximum 4 active projects
        long activeProjectCount = projectAssignmentRepository.countActiveAssignmentsByContractor(contractor);
        if (activeProjectCount >= 4) {
            throw new RuntimeException("Contractor '" + (contractor.getFullName() != null ? contractor.getFullName() : contractor.getUsername()) 
                + "' already has " + activeProjectCount + " active projects. Maximum allowed is 4. "
                + "Please complete some projects before assigning new ones.");
        }
            
        // Business Rule 2: One project can only be handled by one contractor at a time
        // Exception: CLOSED projects can have contractors assigned (for record-keeping/reactivation)
        if (project.getStatus() != root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.CLOSED) {
            if (projectAssignmentRepository.hasActiveAssignment(project)) {
                ProjectAssignment existingAssignment = projectAssignmentRepository.findActiveAssignmentByProject(project).orElse(null);
                String assignedTo = existingAssignment != null && existingAssignment.getContractor() != null 
                    ? (existingAssignment.getContractor().getFullName() != null 
                        ? existingAssignment.getContractor().getFullName() 
                        : existingAssignment.getContractor().getUsername())
                    : "another contractor";
                throw new RuntimeException("Project '" + project.getWorkOrderNumber() + "' is already assigned to " + assignedTo 
                    + ". A project can only be assigned to one contractor at a time. "
                    + "Please close or reassign the project first.");
            }
        }
            
        ProjectAssignment assignment = new ProjectAssignment(project, contractor);
        assignment = projectAssignmentRepository.save(assignment);
            
        // Update project status from UNASSIGNED to ASSIGNED if applicable
        if (project.getStatus() == root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.UNASSIGNED) {
            project.setStatus(root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.ASSIGNED);
            projectRepository.save(project);
        }
        
        // Log contractor assignment
        if (admin != null) {
            auditLogService.logContractorAssigned(project, contractor, admin);
        }
            
        return assignment;
    }
    
    /**
     * Unassign a contractor from a project.
     * If this was the only active assignment, set project status back to UNASSIGNED.
     */
    public void unassignContractorFromProject(Long projectId, Long contractorId) {
        unassignContractorFromProject(projectId, contractorId, null);
    }
    
    public void unassignContractorFromProject(Long projectId, Long contractorId, User admin) {
        Project project = getProjectById(projectId);
        Contractor contractor = contractorRepository.findById(contractorId)
            .orElseThrow(() -> new RuntimeException("Contractor not found"));
        
        ProjectAssignment assignment = projectAssignmentRepository.findByProjectAndContractor(project, contractor)
            .orElseThrow(() -> new RuntimeException("Contractor is not assigned to this project"));
        
        // Check if project is CLOSED - cannot unassign from closed projects
        if (project.getStatus() == root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.CLOSED) {
            throw new RuntimeException("Cannot unassign contractor from a closed project");
        }
        
        projectAssignmentRepository.delete(assignment);
        
        // If no more active assignments, set project status back to UNASSIGNED
        if (!projectAssignmentRepository.hasActiveAssignment(project)) {
            project.setStatus(root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.UNASSIGNED);
            projectRepository.save(project);
        }
        
        // Log contractor unassignment
        if (admin != null) {
            auditLogService.logContractorUnassigned(project, contractor, admin);
        }
    }
    
    public List<ProjectAssignment> getProjectAssignments(Long projectId) {
        Project project = getProjectById(projectId);
        return projectAssignmentRepository.findByProject(project);
    }
    
    public List<Contractor> getContractorsForProject(Long projectId) {
        return getProjectAssignments(projectId).stream()
            .map(ProjectAssignment::getContractor)
            .toList();
    }
    
    public List<Project> getProjectsForContractor(Long contractorId) {
        Contractor contractor = contractorRepository.findById(contractorId)
            .orElseThrow(() -> new RuntimeException("Contractor not found"));
        return projectAssignmentRepository.findByContractor(contractor).stream()
            .map(ProjectAssignment::getProject)
            .toList();
    }
        
    public Project updateProject(Long projectId, String workOrderNumber, String location, String clientCode, String description,
                               String ppwNumber, String workType, String workDetails, String clientCompany,
                               String customer, String loanNumber, String loanType, String address,
                               LocalDate receivedDate, LocalDate dueDate, String assignedTo, String woAdmin,
                               java.math.BigDecimal invoicePrice) {
        return updateProject(projectId, workOrderNumber, location, clientCode, description,
                           ppwNumber, workType, workDetails, clientCompany,
                           customer, loanNumber, loanType, address,
                           receivedDate, dueDate, assignedTo, woAdmin, invoicePrice, null);
    }
        
    public Project updateProject(Long projectId, String workOrderNumber, String location, String clientCode, String description,
                               String ppwNumber, String workType, String workDetails, String clientCompany,
                               String customer, String loanNumber, String loanType, String address,
                               LocalDate receivedDate, LocalDate dueDate, String assignedTo, String woAdmin,
                               java.math.BigDecimal invoicePrice, User updatedBy) {
        Project existingProject = getProjectById(projectId);
            
        // Track changes
        Map<String, String> changes = new HashMap<>();
            
        if (!existingProject.getWorkOrderNumber().equals(workOrderNumber)) {
            if (projectRepository.existsByWorkOrderNumber(workOrderNumber)) {
                throw new RuntimeException("Work order number already exists");
            }
            changes.put("workOrderNumber", existingProject.getWorkOrderNumber() + " → " + workOrderNumber);
        }
        if (!java.util.Objects.equals(existingProject.getLocation(), location)) {
            changes.put("location", existingProject.getLocation() + " → " + location);
        }
        if (!java.util.Objects.equals(existingProject.getClientCode(), clientCode)) {
            changes.put("clientCode", existingProject.getClientCode() + " → " + clientCode);
        }
        if (!java.util.Objects.equals(existingProject.getDescription(), description)) {
            changes.put("description", "changed");
        }
        if (!java.util.Objects.equals(existingProject.getPpwNumber(), ppwNumber)) {
            changes.put("ppwNumber", existingProject.getPpwNumber() + " → " + ppwNumber);
        }
        if (!java.util.Objects.equals(existingProject.getWorkType(), workType)) {
            changes.put("workType", existingProject.getWorkType() + " → " + workType);
        }
        if (!java.util.Objects.equals(existingProject.getWorkDetails(), workDetails)) {
            changes.put("workDetails", "changed");
        }
        if (!java.util.Objects.equals(existingProject.getClientCompany(), clientCompany)) {
            changes.put("clientCompany", existingProject.getClientCompany() + " → " + clientCompany);
        }
        if (!java.util.Objects.equals(existingProject.getCustomer(), customer)) {
            changes.put("customer", existingProject.getCustomer() + " → " + customer);
        }
        if (!java.util.Objects.equals(existingProject.getLoanNumber(), loanNumber)) {
            changes.put("loanNumber", existingProject.getLoanNumber() + " → " + loanNumber);
        }
        if (!java.util.Objects.equals(existingProject.getLoanType(), loanType)) {
            changes.put("loanType", existingProject.getLoanType() + " → " + loanType);
        }
        if (!java.util.Objects.equals(existingProject.getAddress(), address)) {
            changes.put("address", "changed");
        }
        if (!java.util.Objects.equals(existingProject.getReceivedDate(), receivedDate)) {
            changes.put("receivedDate", String.valueOf(existingProject.getReceivedDate()) + " → " + String.valueOf(receivedDate));
        }
        if (!java.util.Objects.equals(existingProject.getDueDate(), dueDate)) {
            changes.put("dueDate", String.valueOf(existingProject.getDueDate()) + " → " + String.valueOf(dueDate));
        }
        if (!java.util.Objects.equals(existingProject.getAssignedTo(), assignedTo)) {
            changes.put("assignedTo", existingProject.getAssignedTo() + " → " + assignedTo);
        }
        if (!java.util.Objects.equals(existingProject.getWoAdmin(), woAdmin)) {
            changes.put("woAdmin", existingProject.getWoAdmin() + " → " + woAdmin);
        }
        if (!java.util.Objects.equals(existingProject.getInvoicePrice(), invoicePrice)) {
            changes.put("invoicePrice", String.valueOf(existingProject.getInvoicePrice()) + " → " + String.valueOf(invoicePrice));
        }
            
        existingProject.setWorkOrderNumber(workOrderNumber);
        existingProject.setLocation(location);
        existingProject.setClientCode(clientCode);
        existingProject.setDescription(description);
        existingProject.setPpwNumber(ppwNumber);
        existingProject.setWorkType(workType);
        existingProject.setWorkDetails(workDetails);
        existingProject.setClientCompany(clientCompany);
        existingProject.setCustomer(customer);
        existingProject.setLoanNumber(loanNumber);
        existingProject.setLoanType(loanType);
        existingProject.setAddress(address);
        existingProject.setReceivedDate(receivedDate);
        existingProject.setDueDate(dueDate);
        existingProject.setAssignedTo(assignedTo);
        existingProject.setWoAdmin(woAdmin);
        existingProject.setInvoicePrice(invoicePrice);
            
        Project savedProject = projectRepository.save(existingProject);
            
        // Log project update if there were changes and an admin was provided
        if (!changes.isEmpty() && updatedBy != null) {
            auditLogService.logProjectUpdated(savedProject, updatedBy, changes);
        }
            
        return savedProject;
    }
    
    public List<Project> searchProjects(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProjects();
        }
        return projectRepository.searchProjects(searchTerm.trim());
    }
    
    public List<Project> advancedSearch(ProjectSearchCriteria criteria) {
        if (criteria.isEmpty()) {
            return getAllProjects();
        }
        
        Specification<Project> spec = ProjectSpecifications.buildSpecification(criteria);
        return projectRepository.findAll(spec);
    }
    
    /**
     * Get the count of active (non-CLOSED) projects for a contractor
     */
    public long getActiveProjectCount(Contractor contractor) {
        return projectAssignmentRepository.countActiveAssignmentsByContractor(contractor);
    }
    
    /**
     * Check if a contractor can take more projects (has fewer than 4 active projects)
     */
    public boolean canContractorTakeMoreProjects(Contractor contractor) {
        return getActiveProjectCount(contractor) < 4;
    }
    
    /**
     * Check if a project is available for assignment (not actively assigned to anyone).
     * CLOSED projects are always available for assignment (for record-keeping/reactivation purposes).
     */
    public boolean isProjectAvailableForAssignment(Project project) {
        // CLOSED projects can always have contractors assigned
        if (project.getStatus() == root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.CLOSED) {
            return true;
        }
        return !projectAssignmentRepository.hasActiveAssignment(project);
    }
    
    /**
     * Delete a project and all its associated data.
     * This includes photos, assignments, view logs, messages, and physical files.
     * This method should only be called in development mode.
     */
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = getProjectById(projectId);
        
        logger.warn("Deleting project {} ({}) and all associated data", project.getId(), project.getWorkOrderNumber());
        
        // Get all photos to delete physical files
        List<Photo> photos = photoRepository.findByProject(project);
        
        // Delete physical photo files
        for (Photo photo : photos) {
            deletePhotoFile(photo.getWebpPath());
            deletePhotoFile(photo.getOriginalPath());
        }
        
        // Delete photo records from database
        photoRepository.deleteByProject(project);
        
        // Delete project view logs
        projectViewLogRepository.deleteByProject(project);
        
        // Delete project messages
        projectMessageRepository.deleteByProject(project);
        
        // Delete project assignments
        projectAssignmentRepository.deleteByProject(project);
        
        // Delete the project itself
        projectRepository.delete(project);
        
        logger.info("Successfully deleted project {} and {} photos", project.getWorkOrderNumber(), photos.size());
    }
    
    private void deletePhotoFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                logger.debug("Deleted file: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filePath, e);
        }
    }
}
