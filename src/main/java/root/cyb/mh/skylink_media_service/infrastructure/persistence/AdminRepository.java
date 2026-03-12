package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
