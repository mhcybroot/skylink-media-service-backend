package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    
    public Admin() {
        super();
    }
    
    public Admin(String username, String password) {
        super(username, password);
    }
    
    @Override
    public String getRole() {
        return "ADMIN";
    }
}
