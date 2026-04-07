package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.AdminChatReadLog;
import root.cyb.mh.skylink_media_service.domain.entities.Project;

import java.util.Optional;

@Repository
public interface AdminChatReadLogRepository extends JpaRepository<AdminChatReadLog, Long> {

    Optional<AdminChatReadLog> findByProjectAndAdminUsername(Project project, String adminUsername);

    void deleteByProject(Project project);
}
