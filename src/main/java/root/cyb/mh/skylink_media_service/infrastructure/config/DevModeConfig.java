package root.cyb.mh.skylink_media_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class DevModeConfig {
    
    /**
     * Whether development mode is enabled.
     * When true, enables features like project deletion that should only be available in development.
     * Should be set to false in production environments.
     */
    private boolean dev = false;
    
    public boolean isDev() {
        return dev;
    }
    
    public void setDev(boolean dev) {
        this.dev = dev;
    }
}
