package root.cyb.mh.skylink_media_service.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
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

import java.util.Optional;

@Component
public class AuthenticationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventListener.class);

    @Autowired
    private LoginAuditLogRepository loginAuditLogRepository;

    @Autowired
    private SystemAuditLogRepository systemAuditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private UserPresenceService userPresenceService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = authentication.getName();
        
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String sessionId = request != null ? request.getSession(false) != null ? request.getSession(false).getId() : null : null;

        // Get user details
        Optional<User> userOpt = userRepository.findByUsername(username);
        Long userId = null;
        String userType = null;
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userId = user.getId();
            userType = user.getRole();
        }

        // Create login audit log
        LoginAuditLog loginLog = new LoginAuditLog(username, userType, true);
        loginLog.setUserId(userId);
        loginLog.setIpAddress(ipAddress);
        loginLog.setUserAgent(userAgent);
        loginLog.setSessionId(sessionId);
        loginAuditLogRepository.save(loginLog);

        // Create system audit log
        SystemAuditLog systemLog = new SystemAuditLog(
                SystemAuditLog.ActionType.LOGIN_SUCCESS,
                userId,
                userType,
                userId,
                "User logged in: " + username
        );
        systemLog.setActorUsername(username);
        systemLog.setIpAddress(ipAddress);
        systemAuditLogRepository.save(systemLog);

        // Update user presence if service is available
        if (userPresenceService != null && sessionId != null && userOpt.isPresent()) {
            userPresenceService.userConnected(sessionId, userOpt.get());
        }

        logger.info("Login success for user: {} from IP: {}", username, ipAddress);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String failureReason = event.getException() != null ? event.getException().getMessage() : "Unknown";

        HttpServletRequest request = getCurrentRequest();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        // Create login audit log for failed attempt
        LoginAuditLog loginLog = new LoginAuditLog(username, null, false);
        loginLog.setIpAddress(ipAddress);
        loginLog.setUserAgent(userAgent);
        loginLog.setFailureReason(failureReason);
        loginAuditLogRepository.save(loginLog);

        // Create system audit log
        SystemAuditLog systemLog = new SystemAuditLog(
                SystemAuditLog.ActionType.LOGIN_FAILURE,
                null,
                null,
                null,
                "Failed login attempt for: " + username + " - " + failureReason
        );
        systemLog.setActorUsername(username);
        systemLog.setIpAddress(ipAddress);
        systemAuditLogRepository.save(systemLog);

        logger.warn("Login failure for user: {} from IP: {} - Reason: {}", username, ipAddress, failureReason);
    }

    @EventListener
    public void onSessionDestroyed(HttpSessionDestroyedEvent event) {
        String sessionId = event.getId();
        
        // Update user presence
        if (userPresenceService != null) {
            userPresenceService.userDisconnected(sessionId);
        }

        // Log logout for sessions that had an authenticated user
        if (event.getSecurityContexts() != null && !event.getSecurityContexts().isEmpty()) {
            event.getSecurityContexts().forEach(securityContext -> {
                if (securityContext.getAuthentication() != null) {
                    String username = securityContext.getAuthentication().getName();
                    
                    // Create logout log
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    Long userId = userOpt.map(User::getId).orElse(null);
                    String userType = userOpt.map(User::getRole).orElse(null);

                    LoginAuditLog loginLog = loginAuditLogRepository.findBySessionId(sessionId)
                            .stream()
                            .findFirst()
                            .orElse(null);
                    
                    if (loginLog != null) {
                        loginLog.setLogoutTime(java.time.LocalDateTime.now());
                        loginAuditLogRepository.save(loginLog);
                    }

                    // Create system audit log
                    SystemAuditLog systemLog = new SystemAuditLog(
                            SystemAuditLog.ActionType.LOGOUT,
                            userId,
                            userType,
                            userId,
                            "User logged out: " + username
                    );
                    systemLog.setActorUsername(username);
                    systemAuditLogRepository.save(systemLog);

                    logger.info("Logout for user: {} (session destroyed)", username);
                }
            });
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
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
