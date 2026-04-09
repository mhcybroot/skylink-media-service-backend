package root.cyb.mh.skylink_media_service.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import root.cyb.mh.skylink_media_service.domain.entities.*;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserManagementService {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private SystemAuditLogRepository systemAuditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== User Listing ====================

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public List<Contractor> getAllContractors() {
        return contractorRepository.findAll();
    }

    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> getUsersByType(Class<? extends User> userType, Pageable pageable) {
        return userRepository.findByUserType(userType, pageable);
    }

    // ==================== User Blocking ====================

    @Transactional
    public void blockUser(Long userId, Long blockedBy, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user instanceof SuperAdmin) {
            throw new RuntimeException("Cannot block a Super Admin");
        }

        user.setIsBlocked(true);
        user.setBlockedAt(LocalDateTime.now());
        user.setBlockedById(blockedBy);
        user.setBlockReason(reason);
        userRepository.save(user);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.USER_BLOCKED,
                blockedBy,
                user.getClass().getSimpleName(),
                userId,
                "User blocked: " + user.getUsername() + (reason != null ? " - Reason: " + reason : "")
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("User {} blocked by admin {}", userId, blockedBy);
    }

    @Transactional
    public void unblockUser(Long userId, Long unblockedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsBlocked(false);
        user.setBlockedAt(null);
        user.setBlockedById(null);
        user.setBlockReason(null);
        userRepository.save(user);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.USER_UNBLOCKED,
                unblockedBy,
                user.getClass().getSimpleName(),
                userId,
                "User unblocked: " + user.getUsername()
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("User {} unblocked by admin {}", userId, unblockedBy);
    }

    // ==================== User Deletion ====================

    @Transactional
    public void deleteUser(Long userId, Long deletedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user instanceof SuperAdmin) {
            throw new RuntimeException("Cannot delete a Super Admin");
        }

        String username = user.getUsername();
        String userType = user.getClass().getSimpleName();

        // Log the action before deletion
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.USER_DELETED,
                deletedBy,
                userType,
                userId,
                "User deleted: " + username
        );
        systemAuditLogRepository.save(auditLog);

        userRepository.delete(user);

        logger.info("User {} deleted by admin {}", userId, deletedBy);
    }

    // ==================== Admin Management ====================

    @Transactional
    public Admin createAdmin(String username, String password, String email, Long createdBy) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        Admin admin = new Admin(username, passwordEncoder.encode(password), email);
        admin = adminRepository.save(admin);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.ADMIN_CREATED,
                createdBy,
                "Admin",
                admin.getId(),
                "Admin created: " + username
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("Admin {} created by user {}", username, createdBy);
        return admin;
    }

    @Transactional
    public SuperAdmin createSuperAdmin(String username, String password, String email, Long createdBy) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        SuperAdmin superAdmin = new SuperAdmin(username, passwordEncoder.encode(password), email);
        superAdmin = superAdminRepository.save(superAdmin);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.SUPER_ADMIN_CREATED,
                createdBy,
                "SuperAdmin",
                superAdmin.getId(),
                "Super Admin created: " + username
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("Super Admin {} created by user {}", username, createdBy);
        return superAdmin;
    }

    @Transactional
    public Contractor createContractor(String username, String password, String fullName, String email, Long createdBy) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        Contractor contractor = new Contractor(username, passwordEncoder.encode(password), fullName, email);
        contractor = contractorRepository.save(contractor);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.USER_CREATED,
                createdBy,
                "Contractor",
                contractor.getId(),
                "Contractor created: " + username
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("Contractor {} created by user {}", username, createdBy);
        return contractor;
    }

    // ==================== User Retrieval ====================

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ==================== Profile Update ====================

    @Transactional
    public void updateAdminProfile(Long adminId, String email, String avatarPath, Long updatedBy) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        if (email != null) {
            admin.setEmail(email);
        }
        if (avatarPath != null) {
            admin.setAvatarPath(avatarPath);
        }
        adminRepository.save(admin);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.USER_UPDATED,
                updatedBy,
                "Admin",
                adminId,
                "Admin profile updated: " + admin.getUsername()
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("Admin {} profile updated by user {}", adminId, updatedBy);
    }

    @Transactional
    public void updateContractorProfile(Long contractorId, String fullName, String email, String avatarPath, Long updatedBy) {
        Contractor contractor = contractorRepository.findById(contractorId)
                .orElseThrow(() -> new RuntimeException("Contractor not found"));
        
        if (fullName != null && !fullName.trim().isEmpty()) {
            contractor.setFullName(fullName.trim());
        }
        if (email != null) {
            contractor.setEmail(email.trim().isEmpty() ? null : email.trim());
        }
        if (avatarPath != null) {
            contractor.setAvatarPath(avatarPath);
        }
        contractorRepository.save(contractor);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.USER_UPDATED,
                updatedBy,
                "Contractor",
                contractorId,
                "Contractor profile updated: " + contractor.getUsername()
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("Contractor {} profile updated by user {}", contractorId, updatedBy);
    }

    @Transactional
    public void updateSuperAdminProfile(Long superAdminId, String email, String avatarPath, Long updatedBy) {
        SuperAdmin superAdmin = superAdminRepository.findById(superAdminId)
                .orElseThrow(() -> new RuntimeException("Super Admin not found"));
        
        if (email != null) {
            superAdmin.setEmail(email);
        }
        if (avatarPath != null) {
            superAdmin.setAvatarPath(avatarPath);
        }
        superAdminRepository.save(superAdmin);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.USER_UPDATED,
                updatedBy,
                "SuperAdmin",
                superAdminId,
                "Super Admin profile updated: " + superAdmin.getUsername()
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("Super Admin {} profile updated by user {}", superAdminId, updatedBy);
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword, Long resetBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user instanceof SuperAdmin && !user.getId().equals(resetBy)) {
            throw new RuntimeException("Cannot reset another Super Admin's password");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Log the action
        SystemAuditLog auditLog = new SystemAuditLog(
                SystemAuditLog.ActionType.USER_UPDATED,
                resetBy,
                user.getClass().getSimpleName(),
                userId,
                "Password reset for: " + user.getUsername()
        );
        systemAuditLogRepository.save(auditLog);

        logger.info("Password reset for user {} by user {}", userId, resetBy);
    }

    @Transactional
    public void updateUserAvatar(Long userId, String avatarPath, Long updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (avatarPath != null) {
            user.setAvatarPath(avatarPath);
            userRepository.save(user);
        }
    }
}
