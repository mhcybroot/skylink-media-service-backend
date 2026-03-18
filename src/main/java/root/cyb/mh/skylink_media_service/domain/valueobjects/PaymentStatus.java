package root.cyb.mh.skylink_media_service.domain.valueobjects;

public enum PaymentStatus {
    UNPAID("Unpaid", "bg-red-100 text-red-800", "bg-red-400"),
    PARTIAL("Partial", "bg-amber-100 text-amber-800", "bg-amber-400"),
    PAID("Paid", "bg-green-100 text-green-800", "bg-green-400");

    private final String displayName;
    private final String badgeClasses;
    private final String dotClasses;

    PaymentStatus(String displayName, String badgeClasses, String dotClasses) {
        this.displayName = displayName;
        this.badgeClasses = badgeClasses;
        this.dotClasses = dotClasses;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeClasses() { return badgeClasses; }
    public String getDotClasses() { return dotClasses; }
}
