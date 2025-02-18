package es.swapsounds.model;

public class User {
    private String username;
    private String email;
    private String password;
    private String profilePicturePath; 


    public User(String username, String email, String password, String profilePicturePath) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profilePicturePath = profilePicturePath;


    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }
}
