package es.swapsounds.model;


public class Comment {
    private User user;
    private String content;
    private Sound sound;


    public Comment(User user, String content, Sound sound) {
        this.user = user;
        this.content = content;
        this.sound = sound;

    }

    // Getters
    public User getUser() { return user; }
    public String getContent() { return content; }
    public Sound getSound() { return sound; }

}