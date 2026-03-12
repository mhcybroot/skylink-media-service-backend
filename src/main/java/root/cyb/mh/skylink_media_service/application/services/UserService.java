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
    
    public Admin createAdmin(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        Admin admin = new Admin(username, passwordEncoder.encode(password));
        return adminRepository.save(admin);
    }
    
    public Contractor createContractor(String username, String password, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        Contractor contractor = new Contractor(username, passwordEncoder.encode(password), fullName);
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
