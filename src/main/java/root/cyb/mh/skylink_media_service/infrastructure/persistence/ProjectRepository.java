package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByWorkOrderNumber(String workOrderNumber);
    boolean existsByWorkOrderNumber(String workOrderNumber);
}
