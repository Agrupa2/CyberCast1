package es.swapsounds.storage;

import es.swapsounds.model.Comment;
import es.swapsounds.model.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class CommentRepository {
    // Mapa: soundId -> Lista de comentarios
    private final Map<Long, List<Comment>> commentsBySoundId = new ConcurrentHashMap<>();

    /**
     * Agrega un comentario para el sonido dado.
     *
     * @param soundId    ID del sonido
     * @param soundTitle Título del sonido (para referencia)
     * @param content    Contenido del comentario
     * @param user       Usuario que comenta
     * @return El comentario creado
     */
    public Comment addComment(long soundId, String soundTitle, String content, User user) {
        // Genera un ID único para el comentario (puedes ajustar la generación de ID)
        long commentId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        Comment comment = new Comment(commentId, content, user);
        comment.setSoundId(soundId);
        comment.setSoundTitle(soundTitle);
        comment.setCreated(LocalDateTime.now());

        commentsBySoundId
            .computeIfAbsent(soundId, k -> new CopyOnWriteArrayList<>())
            .add(comment);
        return comment;
    }

    /**
     * Edita el comentario con el ID proporcionado si el usuario es el autor.
     *
     * @param soundId   ID del sonido
     * @param commentId ID del comentario
     * @param newContent Nuevo contenido del comentario
     * @param user      Usuario que solicita la edición
     * @return true si la edición fue exitosa, false en caso contrario.
     */
    public boolean editComment(long soundId, long commentId, String newContent, User user) {
        List<Comment> commentList = commentsBySoundId.getOrDefault(soundId, Collections.emptyList());
        Optional<Comment> optComment = commentList.stream()
                .filter(comment -> comment.getCommentId() == commentId && comment.getUser().equals(user))
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
     * Elimina el comentario con el ID proporcionado.
     *
     * @param commentId ID del comentario a eliminar.
     * @return true si se eliminó algún comentario, false si no se encontró.
     */
    public boolean deleteComment(long commentId) {
        boolean removed = false;
        // Recorre todas las listas de comentarios
        for (List<Comment> commentList : commentsBySoundId.values()) {
            removed = commentList.removeIf(comment -> comment.getCommentId() == commentId) || removed;
        }
        return removed;
    }

    /**
     * Busca y devuelve un comentario por su ID.
     *
     * @param commentId ID del comentario.
     * @return Un Optional que contiene el comentario si se encontró.
     */
    public Optional<Comment> findCommentById(long commentId) {
        return commentsBySoundId.values().stream()
                .flatMap(List::stream)
                .filter(comment -> comment.getCommentId() == commentId)
                .findFirst();
    }

    /**
     * Obtiene la lista de comentarios asociados a un sonido.
     *
     * @param soundId ID del sonido.
     * @return Lista de comentarios.
     */
    public List<Comment> getCommentsBySoundId(long soundId) {
        return new ArrayList<>(commentsBySoundId.getOrDefault(soundId, Collections.emptyList()));
    }

    /**
     * Obtiene la lista de comentarios realizados por un usuario.
     *
     * @param userId ID del usuario.
     * @return Lista de comentarios.
     */
    public List<Comment> getCommentsByUserId(long userId) {
        return commentsBySoundId.values().stream()
            .flatMap(List::stream)
            .filter(comment -> comment.getUser().getUserId() == userId) // Ahora usamos getUser().getUserId()
            .collect(Collectors.toList());
    }
    

    public void deleteCommentsByUserId(long userId) {
        commentsBySoundId.values().forEach(comments -> 
            comments.removeIf(comment -> comment.getAuthorId() == userId)
        );
    }
    
}
