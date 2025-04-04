package es.swapsounds.service;

import es.swapsounds.model.Comment;
import es.swapsounds.model.User;
import es.swapsounds.storage.CommentRepository;
import es.swapsounds.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private InMemoryStorage storage;
    @Autowired
    private SoundService soundService;

    @Autowired
    private UserService userService;

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
        // Delegar la creación del comentario al repositorio
        return commentRepository.addComment(soundId, soundTitle , content, currentUser);
    }

    /**
     * Edita un comentario, verificando que el usuario sea el autor.
     */
    public boolean editComment(Long userId, long soundId, long commentId, String newContent) {
        User currentUser = userService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return commentRepository.editComment(soundId, commentId, newContent, currentUser);
    }

    /**
     * Elimina un comentario, validando que el usuario sea el autor.
     */
    public void deleteComment(Long userId, long soundId, long commentId) {
        Comment comment = commentRepository.findCommentById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        // Aquí delegamos la validación en el servicio, en lugar de en el controlador.
        if (comment.getAuthorId() != userId) {
            throw new SecurityException("No tienes permiso para eliminar este comentario");
        }

        commentRepository.deleteComment(commentId);
    }

    /**
     * Obtiene todos los comentarios realizados por un usuario.
     */
    public List<Comment> getCommentsByUserId(Long userId) {
        return commentRepository.getCommentsByUserId(userId);
    }
}
