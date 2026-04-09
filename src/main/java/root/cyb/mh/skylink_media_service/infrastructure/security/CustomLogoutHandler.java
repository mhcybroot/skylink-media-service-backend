package root.cyb.mh.skylink_media_service.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import root.cyb.mh.skylink_media_service.application.services.UserPresenceService;
import root.cyb.mh.skylink_media_service.domain.entities.LoginAuditLog;
import root.cyb.mh.skylink_media_service.domain.entities.SystemAuditLog;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.LoginAuditLogRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.SystemAuditLogRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutHandler.class);

    @Autowired
    private LoginAuditLogRepository loginAuditLogRepository;

    @Autowired
    private SystemAuditLogRepository systemAuditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private UserPresenceService userPresenceService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            return;
        }

        String username = authentication.getName();
        String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : null;
        String ipAddress = getClientIpAddress(request);

        // Get user details
        Optional<User> userOpt = userRepository.findByUsername(username);
        Long userId = userOpt.map(User::getId).orElse(null);
        String userType = userOpt.map(User::getRole).orElse(null);

        // Update login audit log with logout time
        if (sessionId != null) {
            LoginAuditLog loginLog = loginAuditLogRepository.findBySessionId(sessionId)
                    .stream()
                    .findFirst()
                    .orElse(null);
            
            if (loginLog != null) {
                loginLog.setLogoutTime(LocalDateTime.now());
                loginAuditLogRepository.save(loginLog);
            }
        }

        // Create system audit log for logout
        SystemAuditLog systemLog = new SystemAuditLog(
                SystemAuditLog.ActionType.LOGOUT,
                userId,
                userType,
                userId,
                "User logged out: " + username
        );
        systemLog.setActorUsername(username);
        systemLog.setIpAddress(ipAddress);
        systemAuditLogRepository.save(systemLog);

        // Update user presence
        if (userPresenceService != null && sessionId != null) {
            userPresenceService.userDisconnected(sessionId);
        }

        logger.info("User {} logged out from IP: {}", username, ipAddress);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return "Unknown";

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Handle multiple IPs from X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "Unknown";
    }
}
