package root.cyb.mh.skylink_media_service.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import root.cyb.mh.skylink_media_service.application.services.UserService;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ── Admin Profile ─────────────────────────────────────────────────────────

    @GetMapping("/admin/profile")
    public String adminProfile(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("email", user instanceof Admin a ? a.getEmail() : "");
        return "admin/profile";
    }

    @PostMapping("/admin/profile/update")
    public String adminUpdateProfile(@RequestParam(required = false) String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String avatarPath = saveAvatar(avatar, authentication.getName());
            userService.updateProfile(authentication.getName(), email, avatarPath);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/admin/profile/change-password")
    public String adminChangePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("New passwords do not match");
            }
            if (newPassword.length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            userService.changePassword(authentication.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    // ── Contractor Profile ────────────────────────────────────────────────────

    @GetMapping("/contractor/profile")
    public String contractorProfile(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("email", user instanceof Contractor c ? c.getEmail() : "");
        return "contractor/profile";
    }

    @PostMapping("/contractor/profile/update")
    public String contractorUpdateProfile(@RequestParam(required = false) String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String avatarPath = saveAvatar(avatar, authentication.getName());
            userService.updateProfile(authentication.getName(), email, avatarPath);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/contractor/profile";
    }

    @PostMapping("/contractor/profile/change-password")
    public String contractorChangePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("New passwords do not match");
            }
            if (newPassword.length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            userService.changePassword(authentication.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/contractor/profile";
    }

    // ── Shared ────────────────────────────────────────────────────────────────

    private String saveAvatar(MultipartFile avatar, String username) throws IOException {
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
        String filename = "avatar_" + username + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path dest = avatarDir.resolve(filename);
        Files.copy(avatar.getInputStream(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        logger.info("Avatar saved for user {}: {}", username, filename);
        return "avatars/" + filename;
    }
}
