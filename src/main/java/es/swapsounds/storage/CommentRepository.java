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
    // Mapa: soundId -> Lista de comentarios
    private final Map<Long, List<Comment>> commentsBySoundId = new ConcurrentHashMap<>();

    public Comment addComment(Long soundId, String content, User user) {
        Comment comment = new Comment(
                UUID.randomUUID().toString(),
                content,
                user // Autor obtenido de la autenticación
        );

        commentsBySoundId
                .computeIfAbsent(soundId, k -> new CopyOnWriteArrayList<>())
                .add(comment);

        return comment;
    }

    // Resto de métodos (getComments, updateComment, deleteComment)
    // ...

    public List<Comment> getComments(Long soundId) {
        return commentsBySoundId.getOrDefault(soundId, Collections.emptyList());
    }

    public boolean editComment(Long soundId, String commentId, String newContent, User user) {
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

    public boolean deleteComment(Long soundId, String commentId, User user) {
        return commentsBySoundId.getOrDefault(soundId, Collections.emptyList())
                .removeIf(comment -> comment.getId().equals(commentId) &&
                        comment.getUser().equals(user));
    }
}