package es.swapsounds.model;

import java.util.ArrayList;
import java.util.List;

public class Sound {
    private int id;
    private String title;
    private String description;
    private String filePath;
    private String imagePath;
    private int userId;
    private List<Comment> comments;
    private String category;
    private String duration;


    public Sound(String title, String description, String filePath, String imagePath, int userId, String Category, String duration) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.userId = userId;
        this.comments = new ArrayList<>();
        this.category = Category;
        this.duration = duration;
    }

    public Sound(int id, String title, String description, String filePath, String imagePath, String category, int userId) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.category = category;
        this.userId = userId;
    }

    public Sound(int id, String title, String description, String filePath, String imagePath, String category, String duration) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.category = category;
        this.duration = duration;
    }

    public Sound() {
        //TODO Auto-generated constructor stub
    }

    public Sound(int i, String title2, String description2, String audioPath, String imagePath2, Integer userId2) {
        //TODO Auto-generated constructor stub
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getUserId() {
        return userId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getDuration() {
        return duration;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
