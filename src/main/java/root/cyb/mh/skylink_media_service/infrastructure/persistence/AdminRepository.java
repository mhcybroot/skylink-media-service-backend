package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    @Query("SELECT a FROM Admin a WHERE a.email IS NOT NULL AND a.email <> ''")
    List<Admin> findAllWithEmail();
}
