package es.swapsounds.model;

import java.sql.Blob;
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

    @Lob
    private Blob profilePicture;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Sound> sounds;

    @ElementCollection(fetch = FetchType.EAGER)
	private List<String> roles;

    public User() {
    }

    public User(String username, String email, String user_password, Blob profilePicture, String... roles) {
        this.username = username;
        this.email = email;
        this.password = user_password;
        this.profilePicture = profilePicture;
        this.roles = List.of(roles);
    }

    public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
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

    public Blob getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Blob profilePicture) {
        this.profilePicture = profilePicture;
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