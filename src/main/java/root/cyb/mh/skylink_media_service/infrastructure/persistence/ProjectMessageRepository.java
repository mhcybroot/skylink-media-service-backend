package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;
import root.cyb.mh.skylink_media_service.domain.entities.Project;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectMessageRepository extends JpaRepository<ProjectMessage, Long> {

    List<ProjectMessage> findByProjectOrderBySentAtAsc(Project project);

    List<ProjectMessage> findByProjectAndSentAtAfterOrderBySentAtAsc(Project project, LocalDateTime since);
}
