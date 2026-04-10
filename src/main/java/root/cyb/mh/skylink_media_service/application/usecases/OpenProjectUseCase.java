package root.cyb.mh.skylink_media_service.application.usecases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectViewLog;
import root.cyb.mh.skylink_media_service.domain.services.ProjectStatusTransitionService;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectViewLogRepository;

@Service
@Transactional
public class OpenProjectUseCase {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectViewLogRepository projectViewLogRepository;
    
    @Autowired
    private ProjectStatusTransitionService statusTransitionService;
    
    public void openProject(Long projectId, Contractor contractor) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.isBlocked()) {
            throw new RuntimeException("This project is temporarily blocked and cannot be opened right now");
        }
        
        if (!project.isAssignedToContractor(contractor)) {
            throw new RuntimeException("Contractor not assigned to this project");
        }
        
        ProjectViewLog viewLog = projectViewLogRepository
            .findByProjectAndContractor(project, contractor)
            .orElse(null);
        
        boolean isFirstTime = (viewLog == null);
        
        if (isFirstTime) {
            viewLog = new ProjectViewLog(project, contractor);
        } else {
            viewLog.recordView();
        }
        
        projectViewLogRepository.save(viewLog);
        
        statusTransitionService.handleProjectOpened(project, contractor, isFirstTime);
        projectRepository.save(project);
    }
}
