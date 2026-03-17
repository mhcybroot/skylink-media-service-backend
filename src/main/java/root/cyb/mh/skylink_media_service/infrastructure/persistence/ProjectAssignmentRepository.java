package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByContractor(Contractor contractor);
    Page<ProjectAssignment> findByContractor(Contractor contractor, Pageable pageable);
    List<ProjectAssignment> findByProject(Project project);
    Optional<ProjectAssignment> findByProjectAndContractor(Project project, Contractor contractor);
    
    @Query("SELECT pa FROM ProjectAssignment pa WHERE pa.contractor = :contractor AND " +
           "(LOWER(pa.project.workOrderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pa.project.location) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pa.project.clientCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pa.project.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ProjectAssignment> searchAssignmentsByContractor(@Param("contractor") Contractor contractor, @Param("searchTerm") String searchTerm);
}
