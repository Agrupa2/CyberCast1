package es.swapsounds.model;

import java.time.LocalDateTime;

public class Comment {
    private String id; // sirve para poder identificar un comentario.
    private User user; // usuario que pone el comentario
    private String content; // contenido del comentario
    private Sound sound; // sonido al que se refiere el comentario --> no se si habrá que usarlo
    private LocalDateTime created; // Fecha de creación
    private LocalDateTime modified; // Fecha de modificación (si se edita)

    public Comment(String id, String content, User user) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.created = LocalDateTime.now();
        this.modified = null;
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

    // Setters (solo para campos modificables)
    public void setContent(String content) {
        this.content = content;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public int getAuthorId() {
        return this.user.getUserId(); // Asume que User tiene getUserId()
    }
}