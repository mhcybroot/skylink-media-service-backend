package root.cyb.mh.skylink_media_service.application.usecases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import root.cyb.mh.skylink_media_service.application.dto.api.LoginRequest;
import root.cyb.mh.skylink_media_service.application.dto.api.LoginResponse;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.domain.entities.User;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.UserRepository;
import root.cyb.mh.skylink_media_service.infrastructure.security.jwt.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ContractorLoginUseCase {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    public LoginResponse execute(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        
        if (!(user instanceof Contractor)) {
            throw new BadCredentialsException("User is not a contractor");
        }
        
        Contractor contractor = (Contractor) user;
        String token = jwtTokenProvider.generateToken(contractor);
        LocalDateTime expiresAt = jwtTokenProvider.getExpirationFromToken(token);
        Long expiresIn = jwtTokenProvider.getExpirationInSeconds();
        
        return new LoginResponse(
                token,
                contractor.getId(),
                contractor.getFullName(),
                expiresAt,
                expiresIn
        );
    }
}
