package es.swapsounds.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table (name = "Comment_Table") // Table name
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id; 

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // intership with user table
    private User user; // User who made the comment

    @Column(nullable = false, length = 500) // comment content
    private String content;

    @ManyToOne
    @JoinColumn(name = "sound_id", nullable = false) // intership with sound table 
    private Sound sound; // Comentary sound

    @CreationTimestamp //  automatic created date
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // automatic modify date
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    // empty Constructor (for JPA)
    public Comment() {
    }

    // Constructor with parameters
    public Comment(String content, User user, Sound sound) {
        this.content = content;
        this.user = user;
        this.sound = sound;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}