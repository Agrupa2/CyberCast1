package es.swapsounds.model;

import jakarta.persistence.*;
import java.sql.Blob;
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
    private long userId;

    @Lob
    private Blob imageBlob; // Para almacenar la imagen como BLOB
    @Lob
    private Blob audioBlob; // Para almacenar el archivo de audio como BLOB

    @OneToMany(cascade = CascadeType.ALL) 
    private List<Comment> comments;

    private String duration; // Duración del audio (en formato "min:seg")
    private LocalDateTime uploadDate; // Fecha de subida

    @ManyToMany
    private List<Category> categories = new ArrayList<>(); // Categorías asociadas al sonido

    // Constructor con todos los parámetros necesarios (para inicializar todos los campos)
    public Sound(String title, String description, Blob audioBlob, Blob imageBlob, long userId, List<Category> categories, String duration) {
        this.title = title;
        this.description = description;
        this.audioBlob = audioBlob;
        this.imageBlob = imageBlob;
        this.userId = userId;
        this.comments = new ArrayList<>();
        this.categories = categories != null ? categories : new ArrayList<>();
        this.duration = duration;
        this.uploadDate = LocalDateTime.now(); // Fecha de carga en el momento de creación
    }

    // Constructor vacío
    public Sound() {}

    // Getters y Setters

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Blob getAudioBlob() {
        return audioBlob;
    }

    public Blob getImageBlob() {
        return imageBlob;
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

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public List<Category> getCategories() {
        return categories;
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

    public void setAudioBlob(Blob audioBlob) {
        this.audioBlob = audioBlob;
    }

    public void setImageBlob(Blob imageBlob) {
        this.imageBlob = imageBlob;
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

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
