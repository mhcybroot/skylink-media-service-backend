package root.cyb.mh.skylink_media_service.application.usecases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.services.ProjectStatusTransitionService;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;

@Service
@Transactional
public class CompleteProjectUseCase {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectStatusTransitionService statusTransitionService;
    
    public void completeProject(Long projectId, Contractor contractor) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (!project.isAssignedToContractor(contractor)) {
            throw new RuntimeException("Contractor not assigned to this project");
        }
        
        statusTransitionService.handleProjectCompleted(project, contractor);
        projectRepository.save(project);
    }
}
