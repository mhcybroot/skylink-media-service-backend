package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.AdminRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Admin createAdmin(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        Admin admin = new Admin(username, passwordEncoder.encode(password), email);
        return adminRepository.save(admin);
    }

    public void updateAdminEmail(String username, String email) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        admin.setEmail(email);
        adminRepository.save(admin);
    }

    public Admin getAdminByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    public void updateProfile(String username, String email, String avatarPath) {
        root.cyb.mh.skylink_media_service.domain.entities.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (email != null) {
            if (user instanceof Admin admin) admin.setEmail(email);
            else if (user instanceof root.cyb.mh.skylink_media_service.domain.entities.Contractor contractor) contractor.setEmail(email);
        }
        if (avatarPath != null) user.setAvatarPath(avatarPath);
        userRepository.save(user);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        root.cyb.mh.skylink_media_service.domain.entities.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Contractor createContractor(String username, String password, String fullName, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        Contractor contractor = new Contractor(username, passwordEncoder.encode(password), fullName, email);
        return contractorRepository.save(contractor);
    }

    public List<Contractor> getAllContractors() {
        return contractorRepository.findAll();
    }

    public List<Contractor> searchContractors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllContractors();
        }
        return contractorRepository.searchContractors(searchTerm.trim());
    }
}
