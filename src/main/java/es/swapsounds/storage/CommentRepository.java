//para almacenar los comentarios de los sonidos
package es.swapsounds.storage;

import org.springframework.stereotype.Repository;

import es.swapsounds.model.Comment;
import es.swapsounds.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class CommentRepository {
    // Map: soundId -> Comment List
    private final Map<Integer, List<Comment>> commentsBySoundId = new ConcurrentHashMap<>();

    public Comment addComment(int soundId, String content, User user) {
        Comment comment = new Comment(
                UUID.randomUUID().toString(),
                content,
                user);

        commentsBySoundId
                .computeIfAbsent(soundId, k -> new CopyOnWriteArrayList<>())
                .add(comment);

        return comment;
    }

    public List<Comment> getCommentsBySoundId(int soundId) {
        return commentsBySoundId.getOrDefault(soundId, Collections.emptyList());
    }

    public boolean editComment(int soundId, String commentId, String newContent, User user) {
        return commentsBySoundId.getOrDefault(soundId, Collections.emptyList())
                .stream()
                .filter(comment -> comment.getId().equals(commentId) &&
                        comment.getUser().equals(user))
                .findFirst()
                .map(comment -> {
                    comment.setContent(newContent);
                    comment.setModified(LocalDateTime.now());
                    return true;
                })
                .orElse(false);
    }

    public boolean deleteComment(int soundId, String commentId, User user) {
        return commentsBySoundId.getOrDefault(soundId, Collections.emptyList())
                .removeIf(comment -> comment.getId().equals(commentId) &&
                        comment.getUser().equals(user));
    }

    public void deleteCommentsByUserId(int userId) {
        commentsBySoundId.values()
                .forEach(comments -> comments.removeIf(comment -> comment.getUser().getUserId() == userId));
    }
}