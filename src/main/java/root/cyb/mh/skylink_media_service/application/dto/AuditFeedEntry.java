package root.cyb.mh.skylink_media_service.application.dto;

import java.time.LocalDateTime;

public record AuditFeedEntry(
        String source,
        LocalDateTime timestamp,
        String actionType,
        String actionLabel,
        Long actorId,
        String actorUsername,
        String actorRole,
        Long projectId,
        String projectWorkOrder,
        String targetType,
        Long targetId,
        String detailsPreview,
        String detailsFull,
        String ipAddress) {

    public boolean isProjectSource() {
        return "PROJECT".equalsIgnoreCase(source);
    }

    public boolean isSystemSource() {
        return "SYSTEM".equalsIgnoreCase(source);
    }

    public boolean isChatEvent() {
        return "CHAT_MESSAGE_SENT".equals(actionType);
    }

    public boolean hasProject() {
        return projectId != null;
    }

    public boolean hasActor() {
        return actorUsername != null && !actorUsername.isBlank();
    }

    public boolean hasTarget() {
        return targetType != null && !targetType.isBlank();
    }

    public boolean hasFullDetails() {
        return detailsFull != null && !detailsFull.isBlank();
    }

    public boolean hasMoreDetails() {
        return hasFullDetails() && !detailsFull.equals(detailsPreview);
    }

    public String getSourceLabel() {
        return isProjectSource() ? "Project" : "System";
    }

    public String getActorRoleLabel() {
        if (actorRole == null || actorRole.isBlank()) {
            return "";
        }
        return switch (actorRole) {
            case "SUPER_ADMIN" -> "Super Admin";
            case "CONTRACTOR" -> "Contractor";
            case "ADMIN" -> "Admin";
            default -> actorRole;
        };
    }

    public String getActorDisplay() {
        if (!hasActor()) {
            return "System";
        }
        return actorUsername;
    }

    public String getDisplayTarget() {
        if (hasProject()) {
            return projectWorkOrder != null && !projectWorkOrder.isBlank()
                    ? projectWorkOrder
                    : "Project #" + projectId;
        }
        if (hasTarget() && targetId != null) {
            return targetType + " #" + targetId;
        }
        if (hasTarget()) {
            return targetType;
        }
        return "-";
    }

    public String getProjectDetailUrl() {
        return hasProject() ? "/super-admin/projects/" + projectId : null;
    }

    public String getProjectHistoryUrl() {
        return hasProject() ? "/super-admin/projects/" + projectId + "/history" : null;
    }

    public String getProjectChatUrl() {
        return hasProject() && isChatEvent()
                ? "/super-admin/projects/" + projectId + "/chat"
                : null;
    }
}
