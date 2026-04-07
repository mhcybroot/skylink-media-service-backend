package root.cyb.mh.skylink_media_service.application.usecases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.domain.valueobjects.PaymentStatus;
import root.cyb.mh.skylink_media_service.domain.exceptions.InvalidStatusTransitionException;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;

@Service
@Transactional
public class ChangeProjectStatusUseCase {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    /**
     * Change project status - validates that only admin-allowed transitions are performed.
     * Admins can only manually set INFIELD (for rework) or CLOSED (to finalize) 
     * from READY_TO_OFFICE status. All other status changes are handled automatically.
     */
    public void changeProjectStatus(Long projectId, ProjectStatus newStatus, User changedBy) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        ProjectStatus currentStatus = project.getStatus();
        
        // Validate that this is an admin-allowed transition
        if (!currentStatus.isAdminTransition(newStatus)) {
            throw new InvalidStatusTransitionException(
                "Admin can only set status to INFIELD (rework) or CLOSED (finalize) " +
                "from READY_TO_OFFICE status. Current status: " + currentStatus.getDisplayName() + ". " +
                "Other status transitions are handled automatically by the system."
            );
        }
        
        project.setStatus(newStatus);
        project.setStatusUpdatedAt(java.time.LocalDateTime.now());
        project.setStatusUpdatedBy(changedBy);
        projectRepository.save(project);
    }
    
    public void changePaymentStatus(Long projectId, PaymentStatus paymentStatus, User changedBy) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        project.setPaymentStatus(paymentStatus);
        project.setStatusUpdatedBy(changedBy);
        projectRepository.save(project);
    }
    
    public void assignContractorAndUpdateStatus(Long projectId, Long contractorId, User admin) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (project.getStatus() == ProjectStatus.UNASSIGNED) {
            project.setStatus(ProjectStatus.ASSIGNED);
            project.setStatusUpdatedAt(java.time.LocalDateTime.now());
            project.setStatusUpdatedBy(admin);
            projectRepository.save(project);
        }
    }
}
