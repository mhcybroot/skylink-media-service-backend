package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.domain.entities.SystemAuditLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long> {

    Page<SystemAuditLog> findByActorId(Long actorId, Pageable pageable);

    Page<SystemAuditLog> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);

    Page<SystemAuditLog> findByActionType(SystemAuditLog.ActionType actionType, Pageable pageable);

    @Query("SELECT s FROM SystemAuditLog s WHERE s.timestamp BETWEEN :start AND :end ORDER BY s.timestamp DESC")
    List<SystemAuditLog> findByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM SystemAuditLog s WHERE (:actionType IS NULL OR s.actionType = :actionType) " +
           "AND (:targetType IS NULL OR s.targetType = :targetType) " +
           "AND (:actorId IS NULL OR s.actorId = :actorId) " +
           "ORDER BY s.timestamp DESC")
    Page<SystemAuditLog> findByFilters(
            @Param("actionType") SystemAuditLog.ActionType actionType,
            @Param("targetType") String targetType,
            @Param("actorId") Long actorId,
            Pageable pageable);

    List<SystemAuditLog> findTop50ByOrderByTimestampDesc();

    long countByActionType(SystemAuditLog.ActionType actionType);

    long countByTimestampAfter(LocalDateTime timestamp);
}
