package root.cyb.mh.skylink_media_service.domain.events;

import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import java.time.LocalDateTime;

public class ProjectCompletedEvent {
    private final Project project;
    private final Contractor contractor;
    private final LocalDateTime completedAt;
    
    public ProjectCompletedEvent(Project project, Contractor contractor) {
        this.project = project;
        this.contractor = contractor;
        this.completedAt = LocalDateTime.now();
    }
    
    public Project getProject() { return project; }
    public Contractor getContractor() { return contractor; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
