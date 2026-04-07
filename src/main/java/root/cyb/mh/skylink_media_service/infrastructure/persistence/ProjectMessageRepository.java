package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;
import root.cyb.mh.skylink_media_service.domain.entities.Project;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectMessageRepository extends JpaRepository<ProjectMessage, Long> {

    List<ProjectMessage> findByProjectOrderBySentAtAsc(Project project);

    @Query("SELECT COUNT(m) FROM ProjectMessage m WHERE m.project = :project AND m.sentAt > :since AND m.sender.username != :username")
    long countUnreadMessages(@Param("project") Project project, @Param("since") LocalDateTime since, @Param("username") String username);

    void deleteByProject(Project project);
}
