package root.cyb.mh.skylink_media_service.domain.events;

import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import java.time.LocalDateTime;

public class ProjectOpenedEvent {
    private final Project project;
    private final Contractor contractor;
    private final LocalDateTime openedAt;
    private final boolean isFirstTime;
    
    public ProjectOpenedEvent(Project project, Contractor contractor, boolean isFirstTime) {
        this.project = project;
        this.contractor = contractor;
        this.openedAt = LocalDateTime.now();
        this.isFirstTime = isFirstTime;
    }
    
    public Project getProject() { return project; }
    public Contractor getContractor() { return contractor; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public boolean isFirstTime() { return isFirstTime; }
}
