package root.cyb.mh.skylink_media_service.application.dto.api;

import jakarta.validation.constraints.NotBlank;

public class ContractorLoginRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;

    public ContractorLoginRequest() {}

    public ContractorLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
