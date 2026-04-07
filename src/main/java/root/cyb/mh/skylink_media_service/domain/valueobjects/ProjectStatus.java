package root.cyb.mh.skylink_media_service.domain.valueobjects;

public enum ProjectStatus {
    UNASSIGNED("Unassigned", "bg-gray-100 text-gray-800", "bg-gray-400"),
    ASSIGNED("Assigned", "bg-blue-100 text-blue-800", "bg-blue-400"),
    UNREAD("Unread", "bg-yellow-100 text-yellow-800", "bg-yellow-400"),
    INFIELD("In Field", "bg-orange-100 text-orange-800", "bg-orange-400"),
    READY_TO_OFFICE("Ready to Office", "bg-purple-100 text-purple-800", "bg-purple-400"),
    CLOSED("Closed", "bg-green-100 text-green-800", "bg-green-400");

    private final String displayName;
    private final String badgeClasses;
    private final String dotClasses;

    ProjectStatus(String displayName, String badgeClasses, String dotClasses) {
        this.displayName = displayName;
        this.badgeClasses = badgeClasses;
        this.dotClasses = dotClasses;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeClasses() { return badgeClasses; }
    public String getDotClasses() { return dotClasses; }

    public boolean canTransitionTo(ProjectStatus newStatus) {
        return switch (this) {
            case UNASSIGNED -> newStatus == ASSIGNED;
            case ASSIGNED -> newStatus == UNREAD;
            case UNREAD -> newStatus == INFIELD;
            case INFIELD -> newStatus == READY_TO_OFFICE;
            case READY_TO_OFFICE -> newStatus == CLOSED || newStatus == INFIELD;
            case CLOSED -> false;
        };
    }

    public boolean isContractorTransition(ProjectStatus newStatus) {
        return switch (this) {
            case ASSIGNED -> newStatus == UNREAD;
            case UNREAD -> newStatus == INFIELD;
            case INFIELD -> newStatus == READY_TO_OFFICE;
            default -> false;
        };
    }

    public boolean isAdminTransition(ProjectStatus newStatus) {
        // Admins can only manually set INFIELD (for rework) or CLOSED (to finalize)
        // from READY_TO_OFFICE status. All other transitions are automatic.
        return switch (this) {
            case READY_TO_OFFICE -> newStatus == CLOSED || newStatus == INFIELD;
            default -> false;
        };
    }
}
