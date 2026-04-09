package root.cyb.mh.skylink_media_service.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAuditLog;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ProjectAuditLogRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Centralized service for logging all project audit events.
 */
@Service
public class AuditLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    
    @Autowired
    private ProjectAuditLogRepository auditLogRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Log a project creation event
     */
    public void logProjectCreated(Project project, User admin) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("workOrderNumber", project.getWorkOrderNumber());
            details.put("location", project.getLocation());
            details.put("clientCode", project.getClientCode());
            
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PROJECT_CREATED,
                admin,
                null,
                project.getWorkOrderNumber(),
                objectMapper.writeValueAsString(details)
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged project creation for project {}", project.getId());
        } catch (Exception e) {
            logger.error("Failed to log project creation: {}", e.getMessage());
        }
    }
    
    /**
     * Log a project update event
     */
    public void logProjectUpdated(Project project, User admin, Map<String, String> changes) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PROJECT_UPDATED,
                admin,
                null,
                null,
                objectMapper.writeValueAsString(changes)
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged project update for project {}", project.getId());
        } catch (Exception e) {
            logger.error("Failed to log project update: {}", e.getMessage());
        }
    }
    
    /**
     * Log a contractor assignment event
     */
    public void logContractorAssigned(Project project, Contractor contractor, User admin) {
        try {
            String contractorName = contractor.getFullName() != null ? contractor.getFullName() : contractor.getUsername();
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.CONTRACTOR_ASSIGNED,
                admin,
                null,
                contractorName,
                "Assigned contractor: " + contractorName + " (ID: " + contractor.getId() + ")"
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged contractor assignment for project {} to contractor {}", project.getId(), contractor.getId());
        } catch (Exception e) {
            logger.error("Failed to log contractor assignment: {}", e.getMessage());
        }
    }
    
    /**
     * Log a contractor unassignment event
     */
    public void logContractorUnassigned(Project project, Contractor contractor, User admin) {
        try {
            String contractorName = contractor.getFullName() != null ? contractor.getFullName() : contractor.getUsername();
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.CONTRACTOR_UNASSIGNED,
                admin,
                contractorName,
                null,
                "Unassigned contractor: " + contractorName + " (ID: " + contractor.getId() + ")"
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged contractor unassignment for project {} from contractor {}", project.getId(), contractor.getId());
        } catch (Exception e) {
            logger.error("Failed to log contractor unassignment: {}", e.getMessage());
        }
    }
    
    /**
     * Log a status change event
     */
    public void logStatusChanged(Project project, String oldStatus, String newStatus, User admin) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.STATUS_CHANGED,
                admin,
                oldStatus,
                newStatus,
                "Status changed from " + oldStatus + " to " + newStatus
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged status change for project {}: {} -> {}", project.getId(), oldStatus, newStatus);
        } catch (Exception e) {
            logger.error("Failed to log status change: {}", e.getMessage());
        }
    }
    
    /**
     * Log a payment status change event
     */
    public void logPaymentStatusChanged(Project project, String oldStatus, String newStatus, User admin) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PAYMENT_STATUS_CHANGED,
                admin,
                oldStatus,
                newStatus,
                "Payment status changed from " + oldStatus + " to " + newStatus
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged payment status change for project {}: {} -> {}", project.getId(), oldStatus, newStatus);
        } catch (Exception e) {
            logger.error("Failed to log payment status change: {}", e.getMessage());
        }
    }
    
    /**
     * Log a project deletion event
     */
    public void logProjectDeleted(Project project, User admin) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PROJECT_DELETED,
                admin,
                project.getWorkOrderNumber(),
                null,
                "Project deleted: " + project.getWorkOrderNumber()
            );
            // Save without project reference since project will be deleted
            auditLogRepository.save(auditLog);
            logger.debug("Logged project deletion for project {}", project.getId());
        } catch (Exception e) {
            logger.error("Failed to log project deletion: {}", e.getMessage());
        }
    }
    
    /**
     * Get all audit logs for a project
     */
    public List<ProjectAuditLog> getAuditLogsForProject(Long projectId) {
        return auditLogRepository.findByProjectIdOrderByTimestampDesc(projectId);
    }
    
    /**
     * Get all audit logs for a project entity
     */
    public List<ProjectAuditLog> getAuditLogsForProject(Project project) {
        return auditLogRepository.findByProjectOrderByTimestampDesc(project);
    }
    
    /**
     * Log a contractor update event (by admin)
     */
    public void logContractorUpdated(Long contractorId, User admin) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                null, // No project associated
                ProjectAuditLog.ActionType.CONTRACTOR_UPDATED,
                admin,
                null,
                String.valueOf(contractorId),
                "Contractor profile updated by admin"
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged contractor update for contractor {}", contractorId);
        } catch (Exception e) {
            logger.error("Failed to log contractor update: {}", e.getMessage());
        }
    }
    
    /**
     * Log a contractor password change event (by admin)
     */
    public void logContractorPasswordChanged(Long contractorId, User admin) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                null,
                ProjectAuditLog.ActionType.CONTRACTOR_PASSWORD_CHANGED,
                admin,
                null,
                null,
                "Contractor password changed by admin (ID: " + contractorId + ")"
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged contractor password change for contractor {}", contractorId);
        } catch (Exception e) {
            logger.error("Failed to log contractor password change: {}", e.getMessage());
        }
    }
}
