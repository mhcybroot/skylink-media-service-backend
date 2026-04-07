package root.cyb.mh.skylink_media_service.infrastructure.persistence;

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
    List<ProjectAssignment> findByProject(Project project);
    Optional<ProjectAssignment> findByProjectAndContractor(Project project, Contractor contractor);
    
    @Query("SELECT pa FROM ProjectAssignment pa WHERE pa.contractor = :contractor AND " +
           "(LOWER(pa.project.workOrderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pa.project.location) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pa.project.clientCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pa.project.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ProjectAssignment> searchAssignmentsByContractor(@Param("contractor") Contractor contractor, @Param("searchTerm") String searchTerm);
    
    /**
     * Count active (non-CLOSED) project assignments for a contractor
     */
    @Query("SELECT COUNT(pa) FROM ProjectAssignment pa WHERE pa.contractor = :contractor AND pa.project.status != root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.CLOSED")
    long countActiveAssignmentsByContractor(@Param("contractor") Contractor contractor);
    
    /**
     * Find active (non-CLOSED) project assignment for a project
     */
    @Query("SELECT pa FROM ProjectAssignment pa WHERE pa.project = :project AND pa.project.status != root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.CLOSED")
    Optional<ProjectAssignment> findActiveAssignmentByProject(@Param("project") Project project);
    
    /**
     * Check if a project has any active (non-CLOSED) assignments
     */
    @Query("SELECT CASE WHEN COUNT(pa) > 0 THEN true ELSE false END FROM ProjectAssignment pa WHERE pa.project = :project AND pa.project.status != root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus.CLOSED")
    boolean hasActiveAssignment(@Param("project") Project project);
    
    /**
     * Delete all assignments for a project
     */
    void deleteByProject(Project project);
}
