package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import root.cyb.mh.skylink_media_service.domain.events.ProjectCompletedEvent;
import root.cyb.mh.skylink_media_service.domain.events.ProjectOpenedEvent;

@Component
public class ProjectActivityAuditListener {

    private final AuditLogService auditLogService;

    public ProjectActivityAuditListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @EventListener
    public void onProjectOpened(ProjectOpenedEvent event) {
        auditLogService.logProjectOpened(
                event.getProject(),
                event.getContractor(),
                event.isFirstTime());
    }

    @EventListener
    public void onProjectCompleted(ProjectCompletedEvent event) {
        auditLogService.logProjectCompleted(
                event.getProject(),
                event.getContractor());
    }
}
