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
    private int id;

    
    public User(String username, String email, String password, String profilePicturePath, int id) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profilePicturePath = profilePicturePath;
        this.comments = new ArrayList<>();
        this.sounds = new ArrayList<>();
        this.id = id;

    }

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
    public int getId() {
        return id;
    }

    public void setId(int id) { 
        this.id = id; 
    }

}
