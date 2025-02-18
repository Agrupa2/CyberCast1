package es.swapsounds.model;


import java.util.ArrayList;
import java.util.List;

public class Sound {
    private String title;
    private String description;
    private String filePath;
    private String imagePath;
    private User uploader;
    private List<Comment> comments;

    public Sound(String title, String description, String filePath, String imagePath, User uploader) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.uploader = uploader;
        this.comments = new ArrayList<>();
    }


    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFilePath() { return filePath; }
    public String getImagePath() { return imagePath; }
    public User getUploader() { return uploader; }
    public List<Comment> getComments() { return comments; }
}
