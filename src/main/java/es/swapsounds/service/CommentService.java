package es.swapsounds.service;

import es.swapsounds.model.Comment;
import es.swapsounds.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private SoundService soundService;

    @Autowired
    private UserService userService;

    private final Map<Long, List<Comment>> commentsBySoundId = new ConcurrentHashMap<>();

    /**
     * Agrega un comentario a un sonido.
     */
    public Comment addComment(Long userId, long soundId, String content) {
        // Obtener el usuario actual
        User currentUser = userService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String soundTitle = soundService.findSoundById(soundId).get().getTitle();
        // Obtener el sonido (para validación, si lo necesitas)
        if (soundService.findSoundById(soundId).isEmpty()) {
            throw new RuntimeException("Sonido no encontrado");
        }

        long commentId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        Comment comment = new Comment(commentId, content, currentUser);
        comment.setSoundId(soundId);
        comment.setSoundTitle(soundTitle);
        comment.setCreated(LocalDateTime.now());

        commentsBySoundId
            .computeIfAbsent(soundId, k -> new CopyOnWriteArrayList<>())
            .add(comment);
        return comment;
    }

    /**
     * Edita un comentario, verificando que el usuario sea el autor.
     */
    public boolean editComment(Long userId, long soundId, long commentId, String newContent) {
        User currentUser = userService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        List<Comment> commentList = commentsBySoundId.getOrDefault(soundId, Collections.emptyList());
        Optional<Comment> optComment = commentList.stream()
                .filter(comment -> comment.getCommentId() == commentId && comment.getUser().equals(currentUser))
                .findFirst();
        if (optComment.isPresent()) {
            Comment comment = optComment.get();
            comment.setContent(newContent);
            comment.setModified(LocalDateTime.now());
            return true;
        }
        return false;
    }

    /**
     * Elimina un comentario, validando que el usuario sea el autor.
     */
    public boolean deleteComment(Long userId, long soundId, long commentId) { //la función del comment repsoitory devuelve un boolean por eso cambuiio de void a boolean
        Comment comment = findCommentById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        // Aquí delegamos la validación en el servicio, en lugar de en el controlador.
        if (comment.getAuthorId() != userId) {
            throw new SecurityException("No tienes permiso para eliminar este comentario");
        }

        boolean removed = false;
        // Recorre todas las listas de comentarios
        for (List<Comment> commentList : commentsBySoundId.values()) {
            removed = commentList.removeIf(comments -> comments.getCommentId() == commentId) || removed; //he tenido que cambiar el nombre de la variable para que no diera error, ya que había una variable local llamada comment
        }
        return removed;
    }

    /**
     * Busca y devuelve un comentario por su ID.
     */
    public Optional<Comment> findCommentById(long commentId) {
        return commentsBySoundId.values().stream()
                .flatMap(List::stream)
                .filter(comment -> comment.getCommentId() == commentId)
                .findFirst();
    }

    /**
     * Obtiene la lista de comentarios asociados a un sonido.
     */
    public List<Comment> getCommentsBySoundId(long soundId) {
        return new ArrayList<>(commentsBySoundId.getOrDefault(soundId, Collections.emptyList()));
    }

    /* Obtiene la lista de comentarios realizados por un usuario.*/

    public List<Comment> getCommentsByUserId(long userId) {
        return commentsBySoundId.values().stream()
            .flatMap(List::stream)
            .filter(comment -> comment.getUser().getUserId() == userId) // Ahora usamos getUser().getUserId()
            .collect(Collectors.toList());
    }

    //*/Borra los comentarios dependiendo del tipo de usuario que seas */

    public void deleteCommentsByUserId(long userId) {
        commentsBySoundId.values().forEach(comments -> 
            comments.removeIf(comment -> comment.getAuthorId() == userId)
        );
    }

    // Borra los comentarios de un sonido específico

    public void deleteCommentsBySoundId(long soundId) {
        commentsBySoundId.values().forEach(comments ->
            comments.removeIf(comment -> comment.getSoundId() == soundId)
        );
    }
}
