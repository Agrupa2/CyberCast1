package es.swapsounds.model;
import jakarta.persistence.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Sound {

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private long soundId;

    private String title;
    private String description;
    private String filePath;
    private String imagePath;
    private long userId;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments;
    
    private String duration;
    private LocalDateTime uploadDate;

    @ManyToMany
    private List<Category> categories = new ArrayList<>();


    public Sound(String title, String description, String filePath, String imagePath, int userId, String Category,
            String duration) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.userId = userId;
        this.comments = new ArrayList<>();
        this.duration = duration;
        this.uploadDate = LocalDateTime.now();
    }

    public Sound(long soundId, String title, String description, String filePath, String imagePath, String category,
                 long userId) {
        this.title = title;
        this.soundId = soundId;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.userId = userId;
        this.uploadDate = LocalDateTime.now();
    }

    public Sound(long id, String title, String description, String filePath, String imagePath, List<Category> categories, String duration) {
        this.soundId = id;
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.imagePath = imagePath;
        this.categories = categories != null ? categories : new ArrayList<>();
        this.duration = duration;
    }

    public Sound() {
    }

    public Sound(long soundId, String title2, String description2, String audioPath, String imagePath2, long userId2,
            String category2, String duration2) {
        this.soundId = soundId;
        this.title = title2;
        this.description = description2;
        this.filePath = audioPath;
        this.imagePath = imagePath2;
        this.userId = userId2;
        this.duration = duration2;
        this.uploadDate = LocalDateTime.now();
    }

    public Sound(long i, String title2, String description2, String audioPath, String imagePath2, User user,
            Object object, String duration2) {
        this.soundId = i;
        this.title = title2;
        this.description = description2;
        this.filePath = audioPath;
        this.imagePath = imagePath2;
        this.userId = user.getUserId();
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

    public long getUserId() {
        return userId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public long getSoundId() {
        return soundId;
    }


    public String getDuration() {
        return duration;
    }

    public void setSoundId(long soundId) {
        this.soundId = soundId;
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

    public void setUserId(long userId) {
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

    public void addCategory(Category category) {
        categories.add(category);
    }

    // Getters
    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}