package root.cyb.mh.skylink_media_service.domain.exceptions;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
    
    public InvalidStatusTransitionException(String currentStatus, String newStatus) {
        super(String.format("Cannot transition from %s to %s", currentStatus, newStatus));
    }
}
