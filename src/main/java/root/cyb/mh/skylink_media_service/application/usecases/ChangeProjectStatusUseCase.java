package root.cyb.mh.skylink_media_service.application.usecases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.domain.valueobjects.PaymentStatus;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectRepository;

@Service
@Transactional
public class ChangeProjectStatusUseCase {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    public void changeProjectStatus(Long projectId, ProjectStatus newStatus, User changedBy) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        project.changeStatus(newStatus, changedBy);
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
            project.changeStatus(ProjectStatus.ASSIGNED, admin);
            projectRepository.save(project);
        }
    }
}
