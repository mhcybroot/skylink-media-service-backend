package root.cyb.mh.skylink_media_service.infrastructure.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import root.cyb.mh.skylink_media_service.application.dto.api.LoginRequest;
import root.cyb.mh.skylink_media_service.application.dto.api.LoginResponse;
import root.cyb.mh.skylink_media_service.application.usecases.ContractorLoginUseCase;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "${cors.allowed-origins:*}")
@Tag(name = "Authentication", description = "Contractor authentication endpoints")
public class AuthApiController {
    
    @Autowired
    private ContractorLoginUseCase contractorLoginUseCase;
    
    @PostMapping("/login")
    @Operation(
        summary = "Contractor Login",
        description = "Authenticate contractor with username and password to receive JWT token. Use the token in the 'Authorize' button above to test protected endpoints."
    )
    @SecurityRequirements
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = contractorLoginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
