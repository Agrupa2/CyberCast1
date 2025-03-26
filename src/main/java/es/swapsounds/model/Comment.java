package es.swapsounds.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; // comment identification
    private User user; // user uploader of the comment
    private String content; // comment content
    private Sound sound; // sound to which the comment is related
    private LocalDateTime created; // comment upload date
    private LocalDateTime modified; // comment modification date
    private long soundId;
    private String soundTitle;


    public Comment(String id, String content, User user) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.created = LocalDateTime.now();
        this.modified = null;
    }

    public Comment() {
    }

    // Getters
    public User getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public long getAuthorId() {
        return this.user.getUserId();
    }

    public long getSoundId() {
        return soundId;
    }

    public void setSoundId(long soundId) {
        this.soundId = soundId;
    }

    public String getSoundTitle() {
        return soundTitle;
    }

    public void setSoundTitle(String soundTitle) {
        this.soundTitle = soundTitle;
    }
}