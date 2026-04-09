package root.cyb.mh.skylink_media_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;
import root.cyb.mh.skylink_media_service.domain.entities.SuperAdmin;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.AdminRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.SuperAdminRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private SuperAdminRepository superAdminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default super-admin if none exists
        if (superAdminRepository.count() == 0) {
            SuperAdmin superAdmin = new SuperAdmin("superadmin", passwordEncoder.encode("superadmin123"));
            superAdminRepository.save(superAdmin);
            System.out.println("Default super-admin created: username=superadmin, password=superadmin123");
        }
        
        // Create default admin if none exists
        if (!userRepository.existsByUsername("admin")) {
            Admin admin = new Admin("admin", passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
            System.out.println("Default admin created: username=admin, password=admin123");
        }
    }
}
