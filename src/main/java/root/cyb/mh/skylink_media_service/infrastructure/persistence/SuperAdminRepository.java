package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.SuperAdmin;

import java.util.Optional;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    
    Optional<SuperAdmin> findByUsername(String username);
    
    boolean existsByUsername(String username);
}
