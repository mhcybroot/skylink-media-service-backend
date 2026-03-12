package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("CONTRACTOR")
public class Contractor extends User {
    
    @OneToMany(mappedBy = "contractor", cascade = CascadeType.ALL)
    private List<ProjectAssignment> assignments;
    
    public Contractor() {
        super();
    }
    
    public Contractor(String username, String password) {
        super(username, password);
    }
    
    @Override
    public String getRole() {
        return "CONTRACTOR";
    }
    
    public List<ProjectAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<ProjectAssignment> assignments) { this.assignments = assignments; }
}
