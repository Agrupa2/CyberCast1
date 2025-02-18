package es.swapsounds.model;


public class Comment {
    private User user;
    private String content;

    public Comment(User user, String content) {
        this.user = user;
        this.content = content;
    }

    // Getters
    public User getUser() { return user; }
    public String getContent() { return content; }
}