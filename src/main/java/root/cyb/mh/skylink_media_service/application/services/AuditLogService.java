package root.cyb.mh.skylink_media_service.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.Photo;
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
    private static final int CHAT_PREVIEW_LENGTH = 120;
    
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
    
    /**
     * Log a project viewed event
     */
    public void logProjectViewed(Project project, User user) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PROJECT_VIEWED,
                user,
                null,
                null,
                "Project viewed: " + project.getWorkOrderNumber()
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged project view for project {} by user {}", project.getId(), user.getId());
        } catch (Exception e) {
            logger.error("Failed to log project view: {}", e.getMessage());
        }
    }

    /**
     * Log a project opened event.
     */
    public void logProjectOpened(Project project, Contractor contractor, boolean isFirstTime) {
        try {
            String actorName = displayName(contractor);
            String details = isFirstTime
                    ? "Project opened for the first time by " + actorName
                    : "Project opened by " + actorName;
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PROJECT_OPENED,
                contractor,
                null,
                null,
                details
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged project opened for project {} by contractor {}", project.getId(), contractor.getId());
        } catch (Exception e) {
            logger.error("Failed to log project opened: {}", e.getMessage());
        }
    }

    /**
     * Log a project completed event.
     */
    public void logProjectCompleted(Project project, Contractor contractor) {
        try {
            String actorName = displayName(contractor);
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PROJECT_COMPLETED,
                contractor,
                null,
                null,
                "Project completed by " + actorName
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged project completed for project {} by contractor {}", project.getId(), contractor.getId());
        } catch (Exception e) {
            logger.error("Failed to log project completed: {}", e.getMessage());
        }
    }

    public void logProjectBlocked(Project project, User admin, String reason) {
        try {
            String details = "Project blocked by " + displayName(admin);
            if (reason != null && !reason.isBlank()) {
                details += " - Reason: " + reason.trim();
            }

            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PROJECT_BLOCKED,
                admin,
                "ACTIVE",
                "BLOCKED",
                details
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged project block for project {} by user {}", project.getId(), admin.getId());
        } catch (Exception e) {
            logger.error("Failed to log project block: {}", e.getMessage());
        }
    }

    public void logProjectUnblocked(Project project, User admin) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PROJECT_UNBLOCKED,
                admin,
                "BLOCKED",
                "ACTIVE",
                "Project unblocked by " + displayName(admin)
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged project unblock for project {} by user {}", project.getId(), admin.getId());
        } catch (Exception e) {
            logger.error("Failed to log project unblock: {}", e.getMessage());
        }
    }
    
    /**
     * Log a chat message sent event
     */
    public void logChatMessageSent(Project project, User user, String messageContent) {
        try {
            String fullMessage = normalizeText(messageContent);
            String preview = previewText(fullMessage, CHAT_PREVIEW_LENGTH);
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.CHAT_MESSAGE_SENT,
                user,
                null,
                preview,
                fullMessage
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged chat message sent for project {} by user {}", project.getId(), user.getId());
        } catch (Exception e) {
            logger.error("Failed to log chat message: {}", e.getMessage());
        }
    }

    /**
     * Log a photo uploaded event.
     */
    public void logPhotoUploaded(Project project, Contractor contractor, Photo photo) {
        try {
            String actorName = displayName(contractor);
            String photoName = photo.getOriginalName() != null && !photo.getOriginalName().isBlank()
                    ? photo.getOriginalName()
                    : photo.getFileName();
            String category = photo.getImageCategory() != null
                    ? photo.getImageCategory().name()
                    : "UNCATEGORIZED";

            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PHOTO_UPLOADED,
                contractor,
                null,
                photoName,
                "Photo uploaded by " + actorName + ": " + photoName + " (" + category + ")"
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged photo upload for project {} by contractor {}", project.getId(), contractor.getId());
        } catch (Exception e) {
            logger.error("Failed to log photo upload: {}", e.getMessage());
        }
    }
    
    /**
     * Log a photos viewed event
     */
    public void logPhotosViewed(Project project, User user, int photoCount) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PHOTOS_VIEWED,
                user,
                null,
                null,
                "Viewed " + photoCount + " photos for project: " + project.getWorkOrderNumber()
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged photos view for project {} by user {}", project.getId(), user.getId());
        } catch (Exception e) {
            logger.error("Failed to log photos view: {}", e.getMessage());
        }
    }
    
    /**
     * Log a photos downloaded event
     */
    public void logPhotosDownloaded(Project project, User user, int photoCount) {
        try {
            ProjectAuditLog auditLog = new ProjectAuditLog(
                project,
                ProjectAuditLog.ActionType.PHOTOS_DOWNLOADED,
                user,
                null,
                null,
                "Downloaded " + photoCount + " photos from project: " + project.getWorkOrderNumber()
            );
            auditLogRepository.save(auditLog);
            logger.debug("Logged photos download for project {} by user {}", project.getId(), user.getId());
        } catch (Exception e) {
            logger.error("Failed to log photos download: {}", e.getMessage());
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace("\r\n", "\n");
    }

    private String previewText(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String displayName(User user) {
        if (user instanceof Contractor contractor
                && contractor.getFullName() != null
                && !contractor.getFullName().isBlank()) {
            return contractor.getFullName();
        }
        return user.getUsername();
    }
}
