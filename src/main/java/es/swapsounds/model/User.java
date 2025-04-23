package es.swapsounds.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private long userId;
    private String username;
    private String email;
    private String password;
    private String profilePicturePath;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Sound> sounds;

    private String role;

    public User() {}

    public User(String username, String email, String password, String profilePicturePath, long userId, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profilePicturePath = profilePicturePath;
        this.comments = new ArrayList<>();
        this.sounds = new ArrayList<>();
        this.userId = userId;
        this.role = role;
    }

    public User(String username2, String email2, String user_password, String photoPath) {
        this.username = username2;
        this.email = email2;
        this.password = user_password;
        this.profilePicturePath = photoPath;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Otros getters y setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getProfilePicturePath() {
        return profilePicturePath != null ? profilePicturePath : "/profiles/default-avatar.png"; // If the user
                                                                                                         // profile
                                                                                                         // image is
                                                                                                         // set to null,
                                                                                                         // use the
                                                                                                         // default
                                                                                                         // profile
                                                                                                         // image
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Sound> getSounds() {
        return sounds;
    }

    public long getUserId() {
        return userId;
    }

    public void setId(long userId) {
        this.userId = userId;
    }

    public void setSound(Sound sound) {
        this.sounds.add(sound);
    }
}