package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.LoginAuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginAuditLogRepository extends JpaRepository<LoginAuditLog, Long> {

    Page<LoginAuditLog> findByUserId(Long userId, Pageable pageable);

    Page<LoginAuditLog> findByUsername(String username, Pageable pageable);

    Page<LoginAuditLog> findBySuccessful(Boolean successful, Pageable pageable);

    List<LoginAuditLog> findBySessionId(String sessionId);

    Optional<LoginAuditLog> findFirstByUserIdOrderByLoginTimeDesc(Long userId);

    @Query("SELECT l FROM LoginAuditLog l WHERE l.loginTime BETWEEN :start AND :end ORDER BY l.loginTime DESC")
    List<LoginAuditLog> findByLoginTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT l FROM LoginAuditLog l WHERE " +
           "(:userType IS NULL OR l.userType = :userType) " +
           "AND (:successful IS NULL OR l.successful = :successful) " +
           "AND (:userId IS NULL OR l.userId = :userId) " +
           "ORDER BY l.loginTime DESC")
    Page<LoginAuditLog> findByFilters(
            @Param("userType") String userType,
            @Param("successful") Boolean successful,
            @Param("userId") Long userId,
            Pageable pageable);

    List<LoginAuditLog> findTop50ByOrderByLoginTimeDesc();

    @Query("SELECT COUNT(l) FROM LoginAuditLog l WHERE l.successful = true AND l.loginTime >= :since")
    long countSuccessfulLoginsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT l.userId) FROM LoginAuditLog l WHERE l.successful = true AND l.loginTime >= :since")
    long countUniqueActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT l.userType, COUNT(l) FROM LoginAuditLog l WHERE l.loginTime >= :since GROUP BY l.userType")
    List<Object[]> countLoginsByUserTypeSince(@Param("since") LocalDateTime since);
}
