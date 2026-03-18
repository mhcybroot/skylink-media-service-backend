package root.cyb.mh.skylink_media_service.infrastructure.web.api;

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
public class AuthApiController {
    
    @Autowired
    private ContractorLoginUseCase contractorLoginUseCase;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = contractorLoginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
