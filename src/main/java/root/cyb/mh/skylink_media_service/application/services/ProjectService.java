package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;
import java.util.List;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;
    
    public Project createProject(String workOrderNumber, String location, String clientCode) {
        if (projectRepository.existsByWorkOrderNumber(workOrderNumber)) {
            throw new RuntimeException("Work order number already exists");
        }
        
        Project project = new Project(workOrderNumber, location, clientCode);
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
        
        // Check if already assigned
        if (projectAssignmentRepository.findByProjectAndContractor(project, contractor).isPresent()) {
            throw new RuntimeException("Contractor already assigned to this project");
        }
        
        ProjectAssignment assignment = new ProjectAssignment(project, contractor);
        return projectAssignmentRepository.save(assignment);
    }
    
    public List<ProjectAssignment> getProjectAssignments(Long projectId) {
        Project project = getProjectById(projectId);
        return projectAssignmentRepository.findByProject(project);
    }
}
