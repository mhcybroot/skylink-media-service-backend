package root.cyb.mh.skylink_media_service.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.domain.entities.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserPresenceService {

    private static final Logger logger = LoggerFactory.getLogger(UserPresenceService.class);

    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();

    public void userConnected(String sessionId, User user) {
        UserSession session = new UserSession(
                sessionId,
                user.getId(),
                user.getUsername(),
                user.getRole(),
                LocalDateTime.now(),
                "/dashboard" // Default page
        );
        activeSessions.put(sessionId, session);
        logger.info("User {} connected with session {}", user.getUsername(), sessionId);
    }

    public void userDisconnected(String sessionId) {
        UserSession session = activeSessions.remove(sessionId);
        if (session != null) {
            logger.info("User {} disconnected (session {})", session.getUsername(), sessionId);
        }
    }

    public void updateUserPage(String sessionId, String currentPage) {
        UserSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.setCurrentPage(currentPage);
            session.setLastActivity(LocalDateTime.now());
        }
    }

    public List<UserPresence> getActiveUsers() {
        return activeSessions.values().stream()
                .map(session -> new UserPresence(
                        session.getUserId(),
                        session.getUsername(),
                        session.getUserType(),
                        session.getCurrentPage(),
                        session.getConnectedAt(),
                        session.getLastActivity()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getUsersByPage() {
        Map<String, Long> pageCounts = new HashMap<>();
        activeSessions.values().forEach(session -> {
            String page = session.getCurrentPage() != null ? session.getCurrentPage() : "unknown";
            pageCounts.merge(page, 1L, Long::sum);
        });
        return pageCounts;
    }

    public int getActiveUserCount() {
        return activeSessions.size();
    }

    public List<UserPresence> getUsersOnPage(String page) {
        return activeSessions.values().stream()
                .filter(session -> page.equals(session.getCurrentPage()))
                .map(session -> new UserPresence(
                        session.getUserId(),
                        session.getUsername(),
                        session.getUserType(),
                        session.getCurrentPage(),
                        session.getConnectedAt(),
                        session.getLastActivity()
                ))
                .collect(Collectors.toList());
    }

    // Inner class for session tracking
    private static class UserSession {
        private final String sessionId;
        private final Long userId;
        private final String username;
        private final String userType;
        private final LocalDateTime connectedAt;
        private LocalDateTime lastActivity;
        private String currentPage;

        public UserSession(String sessionId, Long userId, String username, String userType, 
                          LocalDateTime connectedAt, String currentPage) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.username = username;
            this.userType = userType;
            this.connectedAt = connectedAt;
            this.currentPage = currentPage;
            this.lastActivity = connectedAt;
        }

        public String getSessionId() { return sessionId; }
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getUserType() { return userType; }
        public LocalDateTime getConnectedAt() { return connectedAt; }
        public LocalDateTime getLastActivity() { return lastActivity; }
        public String getCurrentPage() { return currentPage; }
        public void setCurrentPage(String currentPage) { this.currentPage = currentPage; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    }

    // DTO for user presence info
    public static class UserPresence {
        private final Long userId;
        private final String username;
        private final String userType;
        private final String currentPage;
        private final LocalDateTime connectedAt;
        private final LocalDateTime lastActivity;

        public UserPresence(Long userId, String username, String userType, 
                           String currentPage, LocalDateTime connectedAt, LocalDateTime lastActivity) {
            this.userId = userId;
            this.username = username;
            this.userType = userType;
            this.currentPage = currentPage;
            this.connectedAt = connectedAt;
            this.lastActivity = lastActivity;
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getUserType() { return userType; }
        public String getCurrentPage() { return currentPage; }
        public LocalDateTime getConnectedAt() { return connectedAt; }
        public LocalDateTime getLastActivity() { return lastActivity; }
    }
}
