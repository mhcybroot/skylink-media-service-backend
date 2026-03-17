package root.cyb.mh.skylink_media_service.infrastructure.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import root.cyb.mh.skylink_media_service.application.dto.api.ContractorAuthResponse;
import root.cyb.mh.skylink_media_service.application.dto.api.ContractorLoginRequest;
import root.cyb.mh.skylink_media_service.domain.entities.Contractor;
import root.cyb.mh.skylink_media_service.infrastructure.persistence.ContractorRepository;
import root.cyb.mh.skylink_media_service.infrastructure.security.jwt.JwtProperties;
import root.cyb.mh.skylink_media_service.infrastructure.security.jwt.JwtTokenProvider;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Contractor authentication endpoints")
public class AuthApiController {
    private final ContractorRepository contractorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    public AuthApiController(ContractorRepository contractorRepository, 
                            PasswordEncoder passwordEncoder,
                            JwtTokenProvider tokenProvider,
                            JwtProperties jwtProperties) {
        this.contractorRepository = contractorRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    @Operation(summary = "Contractor login", description = "Authenticate contractor and receive JWT token")
    public ResponseEntity<ContractorAuthResponse> login(@Valid @RequestBody ContractorLoginRequest request) {
        Contractor contractor = contractorRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), contractor.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = tokenProvider.generateToken(contractor);
        
        ContractorAuthResponse.ContractorInfo info = new ContractorAuthResponse.ContractorInfo(
                contractor.getId(),
                contractor.getUsername(),
                contractor.getFullName(),
                contractor.getRole()
        );

        ContractorAuthResponse response = new ContractorAuthResponse(
                token,
                jwtProperties.getExpirationMs() / 1000,
                info
        );

        return ResponseEntity.ok(response);
    }
}
