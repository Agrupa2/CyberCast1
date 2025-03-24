package es.swapsounds.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity(name = "User_Table")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    private String username;
    private String email;
    private String password;
    private String profilePicturePath;
    private String role;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] profilePicture;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) //Como le he llamado a la línea 22 de Sound (objeto usuario)
    private List<Sound> sounds;


    protected User() {
        // Used by JPA
    }


    //Constructor: comments, sounds, userId y role
    public User(String username, String email, String password, String profilePicturePath, int userId, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profilePicturePath = profilePicturePath;
        this.comments = new ArrayList<>();
        this.sounds = new ArrayList<>();
        this.userId = userId;
        this.role = role;
    }

    //Constructor: username, email, password y photoPath
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

    public String getPassword() {
        return password;
    }

    public String getProfilePicturePath() {
        return profilePicturePath != null ? profilePicturePath : "/uploads/profiles/default-avatar.png"; // If the user image is set to null, use the default profile image                                                                                              // profile
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

    public void setSound(Sound sound) {
        this.sounds.add(sound);
    }

    public int getUserId() {
        return (int) userId;
    }

    public void setId(int userId) {
        this.userId = userId;
    }

}
