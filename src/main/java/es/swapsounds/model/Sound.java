package es.swapsounds.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ManyToMany;

public class Sound {
    private int id;
    private String title;
    private String description;
    private String filePath;
    private String imagePath;
    private int userId;
    private List<Comment> comments;
    //private List<Category> categories;
    private String duration;
    private LocalDateTime uploadDate;

    @ManyToMany
    private List<Category> categories; //This is the main identity of the relationship

    public Sound(String title, String description, String filePath, String imagePath, int userId, String duration) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.userId = userId;
        this.comments = new ArrayList<>();
        this.duration = duration;
        this.uploadDate = LocalDateTime.now();
        this.categories = new ArrayList<>();
    }

    public Sound(int id, String title, String description, String filePath, String imagePath, int userId) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.userId = userId;
        this.uploadDate = LocalDateTime.now();
        this.categories = new ArrayList<>();
    }

    public Sound(int id, String title, String description, String filePath, String imagePath, String duration) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.duration = duration;
        this.uploadDate = LocalDateTime.now();
        this.categories = new ArrayList<>();
    }

    public Sound() {

    }

    public Sound(int i, String title2, String description2, String audioPath, String imagePath2, Integer userId2, String duration2) {
        this.id = i;
        this.title = title2;
        this.description = description2;
        this.filePath = audioPath;
        this.imagePath = imagePath2;
        this.userId = userId2;
        this.duration = duration2;
        this.uploadDate = LocalDateTime.now();
        this.categories = new ArrayList<>();
    }

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

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void addToCategory(Category category) {
        this.categories.add(category); //This will add the category to the sound
        category.addSound(this); //This will add the sound to the category
    }
}