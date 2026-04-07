package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectViewLog;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;

import java.util.Optional;

@Repository
public interface ProjectViewLogRepository extends JpaRepository<ProjectViewLog, Long> {
    
    Optional<ProjectViewLog> findByProjectAndContractor(Project project, Contractor contractor);
    
    @Query("SELECT COUNT(pvl) > 0 FROM ProjectViewLog pvl WHERE pvl.project = :project AND pvl.contractor = :contractor")
    boolean hasContractorViewedProject(@Param("project") Project project, @Param("contractor") Contractor contractor);
    
    /**
     * Delete all view logs for a project
     */
    void deleteByProject(Project project);
}
