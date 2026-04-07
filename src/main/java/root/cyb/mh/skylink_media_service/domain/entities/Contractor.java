package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("CONTRACTOR")
public class Contractor extends User {

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "contractor", cascade = CascadeType.ALL)
    private List<ProjectAssignment> assignments;

    public Contractor() {
        super();
    }

    public Contractor(String username, String password, String fullName) {
        super(username, password);
        this.fullName = fullName;
    }

    public Contractor(String username, String password, String fullName, String email) {
        super(username, password);
        this.fullName = fullName;
        this.email = email;
    }

    @Override
    public String getRole() {
        return "CONTRACTOR";
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<ProjectAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<ProjectAssignment> assignments) { this.assignments = assignments; }
}
