package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, Long> {
    
    Optional<Contractor> findByUsername(String username);
    
    @Query("SELECT c FROM Contractor c WHERE " +
           "LOWER(c.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Contractor> searchContractors(@Param("searchTerm") String searchTerm);
}
