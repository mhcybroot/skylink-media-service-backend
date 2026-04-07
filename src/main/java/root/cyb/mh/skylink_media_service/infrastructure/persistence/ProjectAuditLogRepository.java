package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAuditLog;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.User;

import java.util.List;

@Repository
public interface ProjectAuditLogRepository extends JpaRepository<ProjectAuditLog, Long> {
    
    /**
     * Get all audit logs for a project, ordered by timestamp descending
     */
    List<ProjectAuditLog> findByProjectOrderByTimestampDesc(Project project);
    
    /**
     * Get all audit logs for a project by project ID
     */
    List<ProjectAuditLog> findByProjectIdOrderByTimestampDesc(Long projectId);
    
    /**
     * Get all audit logs by an admin
     */
    List<ProjectAuditLog> findByAdminOrderByTimestampDesc(User admin);
    
    /**
     * Get audit logs for a project filtered by action type
     */
    List<ProjectAuditLog> findByProjectAndActionTypeOrderByTimestampDesc(Project project, ProjectAuditLog.ActionType actionType);
}
