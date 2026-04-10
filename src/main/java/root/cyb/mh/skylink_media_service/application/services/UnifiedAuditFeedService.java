package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.application.dto.AuditActionOption;
import root.cyb.mh.skylink_media_service.application.dto.AuditFeedEntry;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAuditLog;
import root.cyb.mh.skylink_media_service.domain.entities.SystemAuditLog;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UnifiedAuditFeedRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnifiedAuditFeedService {

    private final UnifiedAuditFeedRepository unifiedAuditFeedRepository;

    public UnifiedAuditFeedService(UnifiedAuditFeedRepository unifiedAuditFeedRepository) {
        this.unifiedAuditFeedRepository = unifiedAuditFeedRepository;
    }

    public Page<AuditFeedEntry> getAuditFeed(
            String source,
            String actionType,
            String targetType,
            String actor,
            String projectFilter,
            Pageable pageable) {
        Page<AuditFeedEntry> rawPage = unifiedAuditFeedRepository.findAuditFeed(
                source,
                actionType,
                targetType,
                actor,
                projectFilter,
                pageable);
        return rawPage.map(this::withResolvedLabel);
    }

    public List<AuditFeedEntry> getRecentAuditFeed(
            String source,
            String actionType,
            String targetType,
            String actor,
            String projectFilter,
            int limit) {
        return unifiedAuditFeedRepository.findRecentAuditFeed(
                        source,
                        actionType,
                        targetType,
                        actor,
                        projectFilter,
                        limit)
                .stream()
                .map(this::withResolvedLabel)
                .toList();
    }

    public List<AuditActionOption> getActionOptions() {
        List<AuditActionOption> options = new ArrayList<>();
        for (SystemAuditLog.ActionType type : SystemAuditLog.ActionType.values()) {
            options.add(new AuditActionOption(type.name(), type.getDisplayName()));
        }
        for (ProjectAuditLog.ActionType type : ProjectAuditLog.ActionType.values()) {
            options.add(new AuditActionOption(type.name(), type.getDisplayName()));
        }
        return options;
    }

    private AuditFeedEntry withResolvedLabel(AuditFeedEntry entry) {
        return new AuditFeedEntry(
                entry.source(),
                entry.timestamp(),
                entry.actionType(),
                resolveActionLabel(entry.source(), entry.actionType()),
                entry.actorId(),
                entry.actorUsername(),
                entry.actorRole(),
                entry.projectId(),
                entry.projectWorkOrder(),
                entry.targetType(),
                entry.targetId(),
                entry.detailsPreview(),
                entry.detailsFull(),
                entry.ipAddress());
    }

    private String resolveActionLabel(String source, String actionType) {
        if (actionType == null || actionType.isBlank()) {
            return "Activity";
        }

        if ("PROJECT".equalsIgnoreCase(source)) {
            try {
                return ProjectAuditLog.ActionType.valueOf(actionType).getDisplayName();
            } catch (IllegalArgumentException ignored) {
                return actionType;
            }
        }

        try {
            return SystemAuditLog.ActionType.valueOf(actionType).getDisplayName();
        } catch (IllegalArgumentException ignored) {
            return actionType;
        }
    }
}
