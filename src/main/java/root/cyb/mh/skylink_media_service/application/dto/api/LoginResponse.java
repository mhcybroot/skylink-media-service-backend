package root.cyb.mh.skylink_media_service.application.dto.api;

import java.time.LocalDateTime;

public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private Long contractorId;
    private String fullName;
    private LocalDateTime expiresAt;
    private Long expiresIn;
    
    public LoginResponse() {}
    
    public LoginResponse(String token, Long contractorId, String fullName, LocalDateTime expiresAt, Long expiresIn) {
        this.token = token;
        this.contractorId = contractorId;
        this.fullName = fullName;
        this.expiresAt = expiresAt;
        this.expiresIn = expiresIn;
    }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public Long getContractorId() { return contractorId; }
    public void setContractorId(Long contractorId) { this.contractorId = contractorId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
}
