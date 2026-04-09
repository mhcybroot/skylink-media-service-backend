package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE TYPE(u) = :userType")
    Page<User> findByUserType(@Param("userType") Class<? extends User> userType, Pageable pageable);
    
    long countByIsBlockedTrue();
}
