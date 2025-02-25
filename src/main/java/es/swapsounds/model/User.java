package es.swapsounds.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String password;
    private String profilePicturePath;
    private List<Comment> comments;
    private List<Sound> sounds;
    private int userId;
    private String role; // Añadir campo de rol

    public User(String username, String email, String password, String profilePicturePath, int userId, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profilePicturePath = profilePicturePath;
        this.comments = new ArrayList<>();
        this.sounds = new ArrayList<>();
        this.userId = userId;
        this.role = role; // Inicializar campo de rol
    }

    // Getters y setters para el campo de rol
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

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
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

    public int getUserId() {
        return userId;
    }

    public void setId(int userId) { 
        this.userId = userId; 
    }

    public void setSound(Sound sound) {
        this.sounds.add(sound);
    }
}
