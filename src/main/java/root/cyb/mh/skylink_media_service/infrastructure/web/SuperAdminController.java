package root.cyb.mh.skylink_media_service.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import root.cyb.mh.skylink_media_service.application.services.*;
import root.cyb.mh.skylink_media_service.domain.entities.*;
import root.cyb.mh.skylink_media_service.domain.valueobjects.ProjectStatus;
import root.cyb.mh.skylink_media_service.infrastructure.config.DevModeConfig;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LoginAuditLogRepository loginAuditLogRepository;

    @Autowired
    private SystemAuditLogRepository systemAuditLogRepository;

    @Autowired
    private UserPresenceService userPresenceService;

    @Autowired
    private UnifiedAuditFeedService unifiedAuditFeedService;

    @Autowired
    private DevModeConfig devModeConfig;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Get current user
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

        // Statistics
        long totalAdmins = adminRepository.count();
        long totalContractors = contractorRepository.count();
        long totalSuperAdmins = superAdminRepository.count();
        long totalProjects = projectRepository.count();
        long blockedProjects = projectRepository.countByBlockedTrue();
        long activeProjects = projectRepository.findAll().stream()
                .filter(p -> p.getStatus() != ProjectStatus.CLOSED && !p.isBlocked())
                .count();
        long totalUsers = totalAdmins + totalContractors + totalSuperAdmins;
        long closedProjects = projectRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProjectStatus.CLOSED)
                .count();

        // Recent activities
        var recentActivities = unifiedAuditFeedService.getRecentAuditFeed(null, null, null, null, null, 10);
        List<LoginAuditLog> recentLogins = loginAuditLogRepository.findTop50ByOrderByLoginTimeDesc();
        List<LoginAuditLog> recentLoginPreview = recentLogins.stream()
                .limit(8)
                .toList();

        // Active users
        int activeUsers = userPresenceService.getActiveUserCount();
        Map<String, Long> usersByPage = userPresenceService.getUsersByPage();
        List<DashboardPageStat> topPages = usersByPage.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(5)
                .map(entry -> new DashboardPageStat(
                        entry.getKey(),
                        entry.getValue(),
                        activeUsers > 0 ? Math.max(1L, Math.round((entry.getValue() * 100.0) / activeUsers)) : 0L))
                .toList();

        // Recent logins in last 24 hours
        long recentLogins24h = loginAuditLogRepository.countSuccessfulLoginsSince(LocalDateTime.now().minusHours(24));

        // Blocked users
        long blockedUsers = userRepository.countByIsBlockedTrue();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalContractors", totalContractors);
        model.addAttribute("totalSuperAdmins", totalSuperAdmins);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("activeProjects", activeProjects);
        model.addAttribute("closedProjects", closedProjects);
        model.addAttribute("blockedProjects", blockedProjects);
        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("recentActivityPreview", recentActivities);
        model.addAttribute("recentLogins", recentLogins);
        model.addAttribute("recentLoginPreview", recentLoginPreview);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("usersByPage", usersByPage);
        model.addAttribute("topPages", topPages);
        model.addAttribute("recentLogins24h", recentLogins24h);
        model.addAttribute("blockedUsers", blockedUsers);
        model.addAttribute("isDevMode", devModeConfig.isDev());

        return "super-admin/dashboard";
    }

    // ==================== User Management ====================

    @GetMapping("/users")
    public String listUsers(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            Authentication authentication) {

        // Get current user for sidebar
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users;

        if (userType != null && !userType.isEmpty()) {
            Class<? extends User> typeClass = switch (userType) {
                case "ADMIN" -> Admin.class;
                case "CONTRACTOR" -> Contractor.class;
                case "SUPER_ADMIN" -> SuperAdmin.class;
                default -> User.class;
            };
            users = userRepository.findByUserType(typeClass, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        model.addAttribute("users", users);
        model.addAttribute("userType", userType);
        model.addAttribute("search", search);
        model.addAttribute("totalUsers", users.getTotalElements());

        return "super-admin/users";
    }

    @PostMapping("/users/{id}/block")
    public String blockUser(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            userManagementService.blockUser(id, currentUser.getId(), reason);
            redirectAttributes.addFlashAttribute("success", "User blocked successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/users";
    }

    @PostMapping("/users/{id}/unblock")
    public String unblockUser(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            userManagementService.unblockUser(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "User unblocked successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            userManagementService.deleteUser(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/users";
    }

    // ==================== Admin Creation ====================

    @GetMapping("/create-admin")
    public String createAdminForm(Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);
        return "super-admin/create-admin";
    }

    @PostMapping("/create-admin")
    public String createAdmin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "false") boolean isSuperAdmin,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

            if (isSuperAdmin) {
                userManagementService.createSuperAdmin(username, password, email, currentUser.getId());
                redirectAttributes.addFlashAttribute("success", "Super Admin created successfully");
            } else {
                userManagementService.createAdmin(username, password, email, currentUser.getId());
                redirectAttributes.addFlashAttribute("success", "Admin created successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/users";
    }

    @GetMapping("/create-contractor")
    public String createContractorForm(Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);
        return "super-admin/create-contractor";
    }

    @PostMapping("/create-contractor")
    public String createContractor(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam(required = false) String email,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            userManagementService.createContractor(username, password, fullName, email, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Contractor created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/users";
    }

    // ==================== Audit Logs ====================

    @GetMapping("/audit-logs")
    public String viewAuditLogs(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String project,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model,
            Authentication authentication) {

        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);

        Pageable pageable = PageRequest.of(page, size);
        var logs = unifiedAuditFeedService.getAuditFeed(source, actionType, targetType, actor, project, pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("source", source);
        model.addAttribute("actionType", actionType);
        model.addAttribute("targetType", targetType);
        model.addAttribute("actor", actor);
        model.addAttribute("project", project);
        model.addAttribute("actionTypes", unifiedAuditFeedService.getActionOptions());

        return "super-admin/audit-logs";
    }

    // ==================== Login History ====================

    @GetMapping("/login-history")
    public String viewLoginHistory(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) Boolean successful,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model,
            Authentication authentication) {

        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by("loginTime").descending());
        Page<LoginAuditLog> logs = loginAuditLogRepository.findByFilters(userType, successful, userId, pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("userType", userType);
        model.addAttribute("successful", successful);
        model.addAttribute("userId", userId);

        return "super-admin/login-history";
    }

    // ==================== Live Monitor ====================

    @GetMapping("/live-monitor")
    public String liveMonitor(Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);

        List<UserPresenceService.UserPresence> activeUsers = userPresenceService.getActiveUsers();
        var usersByPage = userPresenceService.getUsersByPage();
        var recentProjectActivities = unifiedAuditFeedService.getRecentAuditFeed("PROJECT", null, null, null, null, 12);

        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("usersByPage", usersByPage);
        model.addAttribute("activeUserCount", activeUsers.size());
        model.addAttribute("recentProjectActivities", recentProjectActivities);

        return "super-admin/live-monitor";
    }

    // ==================== Profile Management ====================

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser); // For sidebar fragment
        model.addAttribute("email", currentUser instanceof SuperAdmin sa ? sa.getEmail() : "");
        return "super-admin/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(required = false) String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            String avatarPath = saveAvatar(avatar, currentUser.getUsername());
            userManagementService.updateSuperAdminProfile(currentUser.getId(), email, avatarPath, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            
            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("New passwords do not match");
            }
            if (newPassword.length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            
            userManagementService.resetUserPassword(currentUser.getId(), newPassword, currentUser.getId());
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/super-admin/profile";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        User editUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Prevent editing other Super Admins
        if (editUser instanceof SuperAdmin && !editUser.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Cannot edit another Super Admin's profile");
        }
        
        model.addAttribute("editUser", editUser);
        model.addAttribute("currentUser", currentUser);
        return "super-admin/edit-user";
    }

    @PostMapping("/admins/{id}/update")
    public String updateAdmin(
            @PathVariable Long id,
            @RequestParam(required = false) String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            String avatarPath = saveAvatar(avatar, "admin_" + id);
            userManagementService.updateAdminProfile(id, email, avatarPath, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Admin profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/users";
    }

    @PostMapping("/contractors/{id}/update")
    public String updateContractor(
            @PathVariable Long id,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            String avatarPath = saveAvatar(avatar, "contractor_" + id);
            userManagementService.updateContractorProfile(id, fullName, email, avatarPath, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Contractor profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetUserPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
            
            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("Passwords do not match");
            }
            if (newPassword.length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            
            userManagementService.resetUserPassword(id, newPassword, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Password reset successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/users";
    }

    // ==================== Helper Methods ====================

    private String saveAvatar(MultipartFile avatar, String identifier) throws IOException {
        if (avatar == null || avatar.isEmpty()) return null;

        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed for avatar");
        }
        if (avatar.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("Avatar must be under 2MB");
        }

        Path avatarDir = Paths.get(uploadDir, "avatars");
        Files.createDirectories(avatarDir);

        String ext = avatar.getOriginalFilename() != null && avatar.getOriginalFilename().contains(".")
                ? avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf("."))
                : ".jpg";
        String filename = "avatar_" + identifier + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path dest = avatarDir.resolve(filename);
        Files.copy(avatar.getInputStream(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return "avatars/" + filename;
    }

    private record DashboardPageStat(String pagePath, long userCount, long sharePercent) {
    }
}
