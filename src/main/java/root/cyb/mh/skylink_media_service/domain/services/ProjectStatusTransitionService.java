package root.cyb.mh.skylink_media_service.domain.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.domain.events.ProjectOpenedEvent;
import root.cyb.mh.skylink_media_service.domain.events.ProjectCompletedEvent;
import root.cyb.mh.skylink_media_service.domain.exceptions.InvalidStatusTransitionException;

@Service
public class ProjectStatusTransitionService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public ProjectStatusTransitionService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void handleProjectOpened(Project project, Contractor contractor, boolean isFirstTime) {
        if (isFirstTime && project.getStatus() == ProjectStatus.ASSIGNED) {
            project.changeStatus(ProjectStatus.UNREAD, contractor);
            project.setFirstOpenedAt(java.time.LocalDateTime.now());
        }
        
        if (project.getStatus() == ProjectStatus.UNREAD) {
            project.changeStatus(ProjectStatus.INFIELD, contractor);
        }
        
        eventPublisher.publishEvent(new ProjectOpenedEvent(project, contractor, isFirstTime));
    }
    
    public void handleProjectCompleted(Project project, Contractor contractor) {
        if (project.getStatus() != ProjectStatus.INFIELD) {
            throw new InvalidStatusTransitionException(
                project.getStatus().name(), 
                ProjectStatus.READY_TO_OFFICE.name()
            );
        }
        
        project.changeStatus(ProjectStatus.READY_TO_OFFICE, contractor);
        project.setCompletedAt(java.time.LocalDateTime.now());
        project.setCompletedBy(contractor);
        
        eventPublisher.publishEvent(new ProjectCompletedEvent(project, contractor));
    }
    
    public boolean canContractorTransition(Project project, ProjectStatus newStatus, Contractor contractor) {
        return project.getStatus().isContractorTransition(newStatus) && 
               project.isAssignedToContractor(contractor);
    }
}
