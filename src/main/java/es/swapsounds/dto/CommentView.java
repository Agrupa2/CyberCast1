package es.swapsounds.dto;

import java.time.LocalDateTime;

import es.swapsounds.model.Comment;
import es.swapsounds.model.User;

public class CommentView {
    private String id;
    private String content;
    private User user;
    private LocalDateTime created;
    private boolean isCommentOwner;

    public CommentView(Comment comment, boolean isCommentOwner) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.user = comment.getUser();
        this.created = comment.getCreated();
        this.isCommentOwner = isCommentOwner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public boolean isCommentOwner() {
        return isCommentOwner;
    }

    public void setCommentOwner(boolean isCommentOwner) {
        this.isCommentOwner = isCommentOwner;
    }
}