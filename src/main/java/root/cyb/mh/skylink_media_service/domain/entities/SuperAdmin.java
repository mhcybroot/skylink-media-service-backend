package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SUPER_ADMIN")
public class SuperAdmin extends User {

    @Column(name = "email")
    private String email;

    public SuperAdmin() {
        super();
    }

    public SuperAdmin(String username, String password) {
        super(username, password);
    }

    public SuperAdmin(String username, String password, String email) {
        super(username, password);
        this.email = email;
    }

    @Override
    public String getRole() {
        return "SUPER_ADMIN";
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
