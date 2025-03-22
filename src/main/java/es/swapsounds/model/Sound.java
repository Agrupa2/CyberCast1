package es.swapsounds.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "sounds")
public class Sound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String filePath;
    private String imagePath;
    private int userId;
    private String category;
    private String duration;
    private LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;


    public Sound(String title, String description, String filePath, String imagePath, int userId, String Category,
            String duration) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.userId = userId;
        this.comments = new ArrayList<>();
        this.category = Category;
        this.duration = duration;
        this.uploadDate = LocalDateTime.now();
    }

    public Sound(Long id, String title, String description, String filePath, String imagePath, String category,
            int userId) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.category = category;
        this.userId = userId;
        this.uploadDate = LocalDateTime.now();
    }

    public Sound(Long id, String title, String description, String filePath, String imagePath, String category,
            String duration) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.category = category;
        this.duration = duration;
        this.uploadDate = LocalDateTime.now();
    }

    public Sound() {
    }

    public Sound(Long i, String title2, String description2, String audioPath, String imagePath2, Integer userId2,
            String category2, String duration2) {
        this.id = i;
        this.title = title2;
        this.description = description2;
        this.filePath = audioPath;
        this.imagePath = imagePath2;
        this.userId = userId2;
        this.category = category2;
        this.duration = duration2;
        this.uploadDate = LocalDateTime.now();
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

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getDuration() {
        return duration;
    }

    public void setId(Long id) {
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

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}