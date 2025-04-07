package es.swapsounds.dto;

import es.swapsounds.model.Comment;
import es.swapsounds.model.User;

public class CommentView {
    private long id;
    private String content;
    private User user;
    private String created;
    private boolean isCommentOwner;

    public CommentView(Comment comment, boolean isCommentOwner) {
        this.id = comment.getCommentId();
        this.content = comment.getContent();
        this.user = comment.getUser();
        this.created = comment.getCreated();
        this.isCommentOwner = isCommentOwner;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public boolean isCommentOwner() {
        return isCommentOwner;
    }

    public void setCommentOwner(boolean isCommentOwner) {
        this.isCommentOwner = isCommentOwner;
    }
}