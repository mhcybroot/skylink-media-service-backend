package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    @Column(name = "email")
    private String email;

    public Admin() {
        super();
    }

    public Admin(String username, String password) {
        super(username, password);
    }

    public Admin(String username, String password, String email) {
        super(username, password);
        this.email = email;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
