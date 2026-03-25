package root.cyb.mh.skylink_media_service.application.usecases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAssignmentRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectViewLogRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetContractorProjectsUseCase {
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;
    
    @Autowired
    private ProjectViewLogRepository projectViewLogRepository;
    
    public List<ProjectAssignment> getProjectsWithActions(Contractor contractor) {
        List<ProjectAssignment> assignments = projectAssignmentRepository.findByContractor(contractor);
        
        // Enrich with action availability
        for (ProjectAssignment assignment : assignments) {
            // This will be used in the UI to show available actions
        }
        
        return assignments;
    }
    
    public Map<Long, String> getAvailableActions(Contractor contractor) {
        List<ProjectAssignment> assignments = projectAssignmentRepository.findByContractor(contractor);
        
        return assignments.stream()
            .collect(Collectors.toMap(
                assignment -> assignment.getProject().getId(),
                assignment -> determineAvailableAction(assignment, contractor)
            ));
    }
    
    private String determineAvailableAction(ProjectAssignment assignment, Contractor contractor) {
        ProjectStatus status = assignment.getProject().getStatus();
        
        if (status == null) {
            return "NONE";
        }
        
        boolean hasViewed = projectViewLogRepository
            .hasContractorViewedProject(assignment.getProject(), contractor);
        
        return switch (status) {
            case ASSIGNED -> hasViewed ? "CONTINUE" : "START";
            case UNREAD -> "CONTINUE";
            case INFIELD -> "COMPLETE";
            case READY_TO_OFFICE -> "COMPLETED";
            case CLOSED -> "CLOSED";
            default -> "NONE";
        };
    }
}
