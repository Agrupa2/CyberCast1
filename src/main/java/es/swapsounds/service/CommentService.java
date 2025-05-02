package es.swapsounds.service;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.CommentRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private SoundService soundService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

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

    /**
     * Obtiene los comentarios de un sonido con imágenes de perfil en Base64 y datos adicionales.
     */
    public List<Map<String, Object>> getCommentsWithImagesBySoundId(long soundId, Long currentUserId) {
        List<Comment> comments = commentRepository.findBySoundId(soundId);
        List<Map<String, Object>> commentsWithImages = new ArrayList<>();
    
        for (Comment comment : comments) {
            // Determinar si el usuario actual es el propietario del comentario
            boolean owner = (currentUserId != null && currentUserId.equals(comment.getUser().getUserId()));
            comment.setCommentOwner(owner);
    
            // Convertir la imagen de perfil (Blob) a Base64
            String profileImageBase64 = null; // Cambiado de "" a null
            boolean hasProfilePicture = false;
            Blob profilePicture = comment.getUser().getProfilePicture();
            if (profilePicture != null) {
                try {
                    byte[] imageBytes = profilePicture.getBytes(1, (int) profilePicture.length());
                    profileImageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
                    hasProfilePicture = true;
                } catch (SQLException e) {
                    System.err.println("Error al convertir el Blob a Base64: " + e.getMessage());
                }
            }
    
            // Calcular la inicial del usuario
            String userInitial = profileService.getUserInitial(comment.getUser());
    
            // Crear un mapa con los datos del comentario
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("commentId", comment.getCommentId());
            commentData.put("user", comment.getUser());
            commentData.put("content", comment.getContent());
            commentData.put("created", comment.getCreated());
            commentData.put("isCommentOwner", comment.isCommentOwner());
            commentData.put("profileImageBase64", profileImageBase64);
            commentData.put("userInitial", userInitial);
            commentData.put("hasProfilePicture", hasProfilePicture); // Nueva bandera
            commentData.put("soundId", soundId);
            commentData.put("username", comment.getUser().getUsername()); // Añadido para {{username}} en HTML
    
            commentsWithImages.add(commentData);
        }
    
        return commentsWithImages;
    }
}

