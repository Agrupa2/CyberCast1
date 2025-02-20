package es.swapsounds.model;


import java.util.ArrayList;
import java.util.List;

public class Sound {
    private int id;
    private String title;
    private String description;
    private String filePath;
    private String imagePath;
    private User uploader;
    private List<Comment> comments;
    private int CategoryId;

    public Sound(String title, String description, String filePath, String imagePath, User uploader, int CategoryId) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.uploader = uploader;
        this.comments = new ArrayList<>();
        this.CategoryId = CategoryId;
    }

    public Sound(int id, String title, String description, String filePath, String imagePath){
        this.title = title;
        this.id = id;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
    }


    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFilePath() { return filePath; }
    public String getImagePath() { return imagePath; }
    public User getUploader() { return uploader; }
    public List<Comment> getComments() { return comments; }
    public int getId() { return id; }
    public int getCategoryId() { return CategoryId; }
}
