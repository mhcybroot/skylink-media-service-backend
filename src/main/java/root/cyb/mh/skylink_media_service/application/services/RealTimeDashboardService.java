package root.cyb.mh.skylink_media_service.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectMessage;

import java.util.HashMap;
import java.util.Map;

@Service
public class RealTimeDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeDashboardService.class);

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserPresenceService userPresenceService;

    public void broadcastUserPresence(String username, String action) {
        if (messagingTemplate == null) {
            logger.debug("WebSocket not available, skipping broadcast");
            return;
        }

        messagingTemplate.convertAndSend("/topic/presence", (Object) createUserPresenceMessage(username, action));
        logger.debug("Broadcast user presence: {} {}", username, action);
    }

    private Map<String, Object> createUserPresenceMessage(String username, String action) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "userPresence");
        message.put("username", username);
        message.put("action", action);
        message.put("activeUsers", userPresenceService.getActiveUserCount());
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    public void broadcastChatMessage(ProjectMessage projectMessage) {
        if (messagingTemplate == null) {
            logger.debug("WebSocket not available, skipping broadcast");
            return;
        }

        messagingTemplate.convertAndSend("/topic/chat", (Object) createChatMessage(projectMessage));
        logger.debug("Broadcast chat message for project {}", projectMessage.getProject().getId());
    }

    private Map<String, Object> createChatMessage(ProjectMessage projectMessage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "chatMessage");
        message.put("projectId", projectMessage.getProject().getId());
        message.put("projectWo", projectMessage.getProject().getWorkOrderNumber());
        message.put("sender", projectMessage.getSender().getUsername());
        message.put("content", projectMessage.getContent());
        message.put("timestamp", projectMessage.getSentAt().toString());
        return message;
    }

    public void broadcastProjectUpdate(Project project, String action) {
        if (messagingTemplate == null) {
            logger.debug("WebSocket not available, skipping broadcast");
            return;
        }

        messagingTemplate.convertAndSend("/topic/projects", (Object) createProjectUpdateMessage(project, action));
        logger.debug("Broadcast project update: {} {}", project.getWorkOrderNumber(), action);
    }

    private Map<String, Object> createProjectUpdateMessage(Project project, String action) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "projectUpdate");
        message.put("projectId", project.getId());
        message.put("workOrderNumber", project.getWorkOrderNumber());
        message.put("action", action);
        message.put("status", project.getStatus() != null ? project.getStatus().name() : null);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    public void broadcastSystemStats(DashboardStats stats) {
        if (messagingTemplate == null) {
            logger.debug("WebSocket not available, skipping broadcast");
            return;
        }

        messagingTemplate.convertAndSend("/topic/dashboard", (Object) createSystemStatsMessage(stats));
        logger.debug("Broadcast system stats");
    }

    private Map<String, Object> createSystemStatsMessage(DashboardStats stats) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "systemStats");
        message.put("totalUsers", stats.getTotalUsers());
        message.put("activeUsers", stats.getActiveUsers());
        message.put("totalProjects", stats.getTotalProjects());
        message.put("activeProjects", stats.getActiveProjects());
        message.put("recentLogins24h", stats.getRecentLogins24h());
        message.put("usersByPage", stats.getUsersByPage());
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    // DTO for dashboard stats
    public static class DashboardStats {
        private long totalUsers;
        private long activeUsers;
        private long totalProjects;
        private long activeProjects;
        private long recentLogins24h;
        private Map<String, Long> usersByPage;

        public DashboardStats() {
            this.usersByPage = new HashMap<>();
        }

        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

        public long getTotalProjects() { return totalProjects; }
        public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }

        public long getActiveProjects() { return activeProjects; }
        public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }

        public long getRecentLogins24h() { return recentLogins24h; }
        public void setRecentLogins24h(long recentLogins24h) { this.recentLogins24h = recentLogins24h; }

        public Map<String, Long> getUsersByPage() { return usersByPage; }
        public void setUsersByPage(Map<String, Long> usersByPage) { this.usersByPage = usersByPage; }
    }
}
