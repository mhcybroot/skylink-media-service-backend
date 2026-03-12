package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.Photo;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByProject(Project project);
    List<Photo> findByContractor(Contractor contractor);
    List<Photo> findByProjectAndContractor(Project project, Contractor contractor);
}
