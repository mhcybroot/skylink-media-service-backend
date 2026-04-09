package root.cyb.mh.skylink_media_service.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "avatar_path")
    private String avatarPath;

    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "blocked_by_id")
    private Long blockedById;

    @Column(name = "block_reason")
    private String blockReason;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    protected User() {}
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }

    public Long getBlockedById() { return blockedById; }
    public void setBlockedById(Long blockedById) { this.blockedById = blockedById; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

    public abstract String getRole();
}
