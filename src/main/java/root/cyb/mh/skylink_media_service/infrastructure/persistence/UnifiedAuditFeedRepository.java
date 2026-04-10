package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import root.cyb.mh.skylink_media_service.application.dto.AuditFeedEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class UnifiedAuditFeedRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UnifiedAuditFeedRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<AuditFeedEntry> findAuditFeed(
            String source,
            String actionType,
            String targetType,
            String actor,
            String projectFilter,
            Pageable pageable) {

        QueryParts queryParts = buildUnionQuery(source, actionType, targetType, actor, projectFilter);
        if (queryParts.sql().isBlank()) {
            return Page.empty(pageable);
        }

        MapSqlParameterSource countParams = new MapSqlParameterSource(queryParts.params().getValues());
        String countSql = "SELECT COUNT(*) FROM (" + queryParts.sql() + ") audit_feed";
        Long total = jdbcTemplate.queryForObject(countSql, countParams, Long.class);

        MapSqlParameterSource pageParams = new MapSqlParameterSource(queryParts.params().getValues());
        pageParams.addValue("limit", pageable.getPageSize());
        pageParams.addValue("offset", pageable.getOffset());

        String pageSql = "SELECT * FROM (" + queryParts.sql() + ") audit_feed " +
                "ORDER BY timestamp DESC LIMIT :limit OFFSET :offset";
        List<AuditFeedEntry> content = jdbcTemplate.query(pageSql, pageParams, AUDIT_FEED_ROW_MAPPER);

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    public List<AuditFeedEntry> findRecentAuditFeed(
            String source,
            String actionType,
            String targetType,
            String actor,
            String projectFilter,
            int limit) {

        QueryParts queryParts = buildUnionQuery(source, actionType, targetType, actor, projectFilter);
        if (queryParts.sql().isBlank()) {
            return List.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource(queryParts.params().getValues());
        params.addValue("limit", limit);

        String sql = "SELECT * FROM (" + queryParts.sql() + ") audit_feed " +
                "ORDER BY timestamp DESC LIMIT :limit";
        return jdbcTemplate.query(sql, params, AUDIT_FEED_ROW_MAPPER);
    }

    private QueryParts buildUnionQuery(
            String source,
            String actionType,
            String targetType,
            String actor,
            String projectFilter) {

        String normalizedSource = normalize(source);
        String normalizedActionType = normalize(actionType);
        String normalizedTargetType = normalize(targetType);
        String normalizedActor = normalize(actor);
        String normalizedProjectFilter = normalize(projectFilter);

        boolean includeProjectFeed = !"SYSTEM".equals(normalizedSource);
        boolean includeSystemFeed = !"PROJECT".equals(normalizedSource) && normalizedProjectFilter == null;

        if (!includeProjectFeed && !includeSystemFeed) {
            return new QueryParts("", new MapSqlParameterSource());
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (normalizedActionType != null) {
            params.addValue("actionType", normalizedActionType);
        }
        if (normalizedTargetType != null) {
            params.addValue("targetType", normalizedTargetType);
        }
        if (normalizedActor != null) {
            params.addValue("actorLike", "%" + normalizedActor.toLowerCase(Locale.ROOT) + "%");
        }
        if (normalizedProjectFilter != null) {
            params.addValue("projectLike", "%" + normalizedProjectFilter.toLowerCase(Locale.ROOT) + "%");
        }

        Long projectExactId = null;
        if (normalizedProjectFilter != null) {
            try {
                projectExactId = Long.parseLong(normalizedProjectFilter);
            } catch (NumberFormatException ignored) {
            }
        }
        if (projectExactId != null) {
            params.addValue("projectExactId", projectExactId);
        }

        List<String> unions = new ArrayList<>();
        if (includeSystemFeed) {
            unions.add(systemAuditSql(normalizedActionType, normalizedTargetType, normalizedActor));
        }
        if (includeProjectFeed) {
            unions.add(projectAuditSql(normalizedActionType, normalizedActor, normalizedProjectFilter, projectExactId));
        }

        return new QueryParts(String.join(" UNION ALL ", unions), params);
    }

    private String systemAuditSql(String actionType, String targetType, String actor) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    'SYSTEM' AS source,
                    s.timestamp AS timestamp,
                    CAST(s.action_type AS VARCHAR) AS action_type,
                    s.actor_id AS actor_id,
                    COALESCE(actor.username, s.actor_username) AS actor_username,
                    actor.user_type AS actor_role,
                    NULL AS project_id,
                    NULL AS project_work_order,
                    s.target_type AS target_type,
                    s.target_id AS target_id,
                    COALESCE(s.details, '') AS details_preview,
                    COALESCE(s.details, '') AS details_full,
                    s.ip_address AS ip_address
                FROM system_audit_log s
                LEFT JOIN users actor ON actor.id = s.actor_id
                WHERE 1 = 1
                """);

        if (actionType != null) {
            sql.append("\n  AND CAST(s.action_type AS VARCHAR) = :actionType");
        }
        if (actor != null) {
            sql.append("\n  AND LOWER(COALESCE(actor.username, s.actor_username, '')) LIKE :actorLike");
        }
        if (targetType != null) {
            sql.append("\n  AND s.target_type = :targetType");
        }

        return sql.toString();
    }

    private String projectAuditSql(String actionType, String actor, String projectFilter, Long projectExactId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    'PROJECT' AS source,
                    p.timestamp AS timestamp,
                    CAST(p.action_type AS VARCHAR) AS action_type,
                    p.admin_id AS actor_id,
                    actor.username AS actor_username,
                    actor.user_type AS actor_role,
                    p.project_id AS project_id,
                    project.work_order_number AS project_work_order,
                    'Project' AS target_type,
                    p.project_id AS target_id,
                    CASE
                        WHEN CAST(p.action_type AS VARCHAR) = 'CHAT_MESSAGE_SENT'
                            THEN COALESCE(NULLIF(p.new_value, ''), NULLIF(p.details, ''), '')
                        ELSE COALESCE(NULLIF(p.details, ''), NULLIF(p.new_value, ''), NULLIF(p.old_value, ''), '')
                    END AS details_preview,
                    CASE
                        WHEN CAST(p.action_type AS VARCHAR) = 'CHAT_MESSAGE_SENT'
                            THEN COALESCE(NULLIF(p.details, ''), NULLIF(p.new_value, ''), '')
                        ELSE COALESCE(NULLIF(p.details, ''), NULLIF(p.new_value, ''), NULLIF(p.old_value, ''), '')
                    END AS details_full,
                    p.ip_address AS ip_address
                FROM project_audit_log p
                LEFT JOIN users actor ON actor.id = p.admin_id
                LEFT JOIN projects project ON project.id = p.project_id
                WHERE p.project_id IS NOT NULL
                """);

        if (actionType != null) {
            sql.append("\n  AND CAST(p.action_type AS VARCHAR) = :actionType");
        }
        if (actor != null) {
            sql.append("\n  AND LOWER(COALESCE(actor.username, '')) LIKE :actorLike");
        }
        if (projectFilter != null) {
            sql.append("\n  AND (");
            if (projectExactId != null) {
                sql.append("p.project_id = :projectExactId OR ");
            }
            sql.append("LOWER(COALESCE(project.work_order_number, '')) LIKE :projectLike)");
        }

        return sql.toString();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final RowMapper<AuditFeedEntry> AUDIT_FEED_ROW_MAPPER = new RowMapper<>() {
        @Override
        public AuditFeedEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long actorId = rs.getLong("actor_id");
            if (rs.wasNull()) {
                actorId = null;
            }

            Long projectId = rs.getLong("project_id");
            if (rs.wasNull()) {
                projectId = null;
            }

            Long targetId = rs.getLong("target_id");
            if (rs.wasNull()) {
                targetId = null;
            }

            return new AuditFeedEntry(
                    rs.getString("source"),
                    rs.getTimestamp("timestamp").toLocalDateTime(),
                    rs.getString("action_type"),
                    rs.getString("action_type"),
                    actorId,
                    rs.getString("actor_username"),
                    rs.getString("actor_role"),
                    projectId,
                    rs.getString("project_work_order"),
                    rs.getString("target_type"),
                    targetId,
                    rs.getString("details_preview"),
                    rs.getString("details_full"),
                    rs.getString("ip_address"));
        }
    };

    private record QueryParts(String sql, MapSqlParameterSource params) {
    }
}
