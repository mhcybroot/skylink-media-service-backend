package root.cyb.mh.skylink_media_service.domain.valueobjects;

public enum ImageCategory {
    BEFORE("Before Image", "bg-amber-100 text-amber-800", "bg-amber-400"),
    DURING("During Image", "bg-blue-100 text-blue-800", "bg-blue-400"),
    AFTER("After Image", "bg-green-100 text-green-800", "bg-green-400"),
    UNCATEGORIZED("Uncategorized", "bg-gray-100 text-gray-800", "bg-gray-400");

    private final String displayName;
    private final String badgeClasses;
    private final String dotClasses;

    ImageCategory(String displayName, String badgeClasses, String dotClasses) {
        this.displayName = displayName;
        this.badgeClasses = badgeClasses;
        this.dotClasses = dotClasses;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeClasses() { return badgeClasses; }
    public String getDotClasses() { return dotClasses; }
}
