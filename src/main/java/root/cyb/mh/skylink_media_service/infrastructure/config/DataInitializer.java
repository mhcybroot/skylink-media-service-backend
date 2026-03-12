package root.cyb.mh.skylink_media_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import root.cyb.mh.skylink_media_service.domain.entities.Admin;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.AdminRepository;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin if none exists
        if (!userRepository.existsByUsername("admin")) {
            Admin admin = new Admin("admin", passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
            System.out.println("Default admin created: username=admin, password=admin123");
        }
    }
}
