package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.application.dto.ProjectSearchCriteria;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;
    
    public Project createProject(String workOrderNumber, String location, String clientCode, String description) {
        return createProject(workOrderNumber, location, clientCode, description, 
                           null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    public Project createProject(String workOrderNumber, String location, String clientCode, String description,
                               String ppwNumber, String workType, String workDetails, String clientCompany,
                               String customer, String loanNumber, String loanType, String address,
                               LocalDate receivedDate, LocalDate dueDate, String assignedTo, String woAdmin,
                               java.math.BigDecimal invoicePrice) {
        if (projectRepository.existsByWorkOrderNumber(workOrderNumber)) {
            throw new RuntimeException("Work order number already exists");
        }
        
        Project project = new Project(workOrderNumber, location, clientCode, description,
                                    ppwNumber, workType, workDetails, clientCompany,
                                    customer, loanNumber, loanType, address,
                                    receivedDate, dueDate, assignedTo, woAdmin, invoicePrice);
        return projectRepository.save(project);
    }
    
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Project not found"));
    }
    
    public ProjectAssignment assignContractorToProject(Long projectId, Long contractorId) {
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
            
        return assignment;
    }
    
    /**
     * Unassign a contractor from a project.
     * If this was the only active assignment, set project status back to UNASSIGNED.
     */
    public void unassignContractorFromProject(Long projectId, Long contractorId) {
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
        Project existingProject = getProjectById(projectId);
        
        if (!existingProject.getWorkOrderNumber().equals(workOrderNumber) && 
            projectRepository.existsByWorkOrderNumber(workOrderNumber)) {
            throw new RuntimeException("Work order number already exists");
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
        
        return projectRepository.save(existingProject);
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
}
