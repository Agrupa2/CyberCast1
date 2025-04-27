package es.swapsounds.service;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.CommentRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private SoundService soundService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Agrega un comentario a un sonido.
     */
    @Transactional
    public Comment addComment(Long userId, long soundId, String content) {
        // Obtener el usuario actual
        User currentUser = userService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Sound sound = soundService.findSoundById(soundId)
            .orElseThrow(() -> new RuntimeException("Sonido no encontrado"));

        String soundTitle = sound.getTitle(); // Obtener el título del sonido

        //Crea un nuevo commentId pero no lo hace maualmente, lo hace la base de datos
        Comment comment = new Comment();
        comment.setContent(content); // Asignar el contenido al comentario
        comment.setUser(currentUser); // Asignar el usuario al comentario
        comment.setSound(sound); // Asignar el sonido al comentario
        comment.setSoundId(soundId);
        comment.setSoundTitle(soundTitle);
        comment.setCreated(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    /**
     * Edita un comentario, verificando que el usuario sea el autor.
     */
    @Transactional
    public boolean editComment(Long userId, long soundId, long commentId, String newContent) {
        User currentUser = userService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        Optional<Comment> optComment = commentRepository.findById(commentId);
        if (optComment.isPresent()) {
            Comment comment = optComment.get();
            if (comment.getUser().getUserId() == userId && comment.getSoundId() == soundId) {
                comment.setContent(newContent);
                comment.setModified(LocalDateTime.now());
                commentRepository.save(comment);
                return true;
            }
    }
        return false;
}

    /**
     * Elimina un comentario, validando que el usuario sea el autor.
     */
    @Transactional
    public boolean deleteComment(Long userId, long soundId, long commentId) { //la función del comment repsoitory devuelve un boolean por eso cambuiio de void a boolean
       
        Optional<Comment> optComment = commentRepository.findById(commentId);
        if (optComment.isPresent()) {
            Comment comment = optComment.get();
            if (comment.getUser().getUserId() == userId) {
                commentRepository.delete(comment);
                return true;
            }
            throw new SecurityException("No tienes permiso para eliminar este comentario");
        }
        return false;
    }

    /**
     * Busca y devuelve un comentario por su ID.
     */

    public Optional<Comment> findCommentById(long commentId) {
        return commentRepository.findById(commentId);
    }

    /**
     * Obtiene la lista de comentarios asociados a un sonido.
     */
    public List<Comment> getCommentsBySoundId(long soundId) {
        return commentRepository.findBySoundId(soundId);
    }

    /* Obtiene la lista de comentarios realizados por un usuario.*/

    public List<Comment> getCommentsByUserId(long userId) {
        return commentRepository.findByUserUserId(userId);
    }

    //*/Borra los comentarios dependiendo del tipo de usuario que seas */

    @Transactional
    public void deleteCommentsByUserId(long userId) {
        commentRepository.deleteByUserUserId(userId);
    }

    // Borra los comentarios de un sonido específico

    @Transactional
    public void deleteCommentsBySoundId(long soundId) {
        commentRepository.deleteBySoundId(soundId);
    }
}
