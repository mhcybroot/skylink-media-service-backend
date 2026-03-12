package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByContractor(Contractor contractor);
    List<ProjectAssignment> findByProject(Project project);
    Optional<ProjectAssignment> findByProjectAndContractor(Project project, Contractor contractor);
}
