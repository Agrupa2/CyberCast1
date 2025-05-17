package es.swapsounds.service;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.CommentRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private CommentRepository commentRepository;

    /**
     * Agrega un comentario a un sonido.
     */
    @Transactional
    public Comment addComment(Long userId, long soundId, String content) {
        // Validar entrada
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El contenido del comentario no puede estar vacío");
        }
        if (content.length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El comentario es demasiado largo (máximo 150 caracteres)");
        }

        // Configurar lista segura para sanitización
        Safelist safelist = Safelist.relaxed()
                .addTags("h1", "h2", "code")
                .addAttributes("a", "href", "target")
                .addAttributes(":all", "class")
                .addProtocols("a", "href", "http", "https");

        // Sanitizar el contenido
        String cleanContent = Jsoup.clean(content, safelist);

        // Obtener el usuario
        User currentUser = userService.findUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Obtener el sonido
        Sound sound = soundService.findSoundById(soundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sonido no encontrado"));

        String soundTitle = sound.getTitle();

        // Crear el comentario
        Comment comment = new Comment();
        comment.setContent(cleanContent);
        comment.setUser(currentUser);
        comment.setSound(sound);
        comment.setSoundId(soundId);
        comment.setSoundTitle(soundTitle);
        comment.setCreated(LocalDateTime.now());

        // Guardar y devolver
        return commentRepository.save(comment);
    }

    /**
     * Edita un comentario, verificando que el usuario sea el autor.
     */
    @Transactional
    public boolean editComment(Long userId, long soundId, long commentId, String newContent) {
        Optional<Comment> optComment = commentRepository.findById(commentId);
        if (optComment.isPresent()) {
            Comment comment = optComment.get();

            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            if ((isAdmin || comment.getUser().getUserId() == userId)
                    && comment.getSoundId() == soundId) {

                Safelist safelist = Safelist.relaxed()
                        .addTags("h1", "h2", "code")
                        .addAttributes("a", "href", "target")
                        .addAttributes(":all", "class")
                        .addProtocols("a", "href", "http", "https");

                // Sanitizar el contenido
                String cleanContent = Jsoup.clean(newContent, safelist);
                comment.setContent(cleanContent);
                comment.setModified(LocalDateTime.now());
                commentRepository.save(comment);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public Comment commentEdition(Long userId, long soundId, long commentId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (comment.getUser().getUserId() != userId || comment.getSoundId() != soundId) {
            throw new SecurityException("No autorizado para editar este comentario");
        }
        
        Safelist safelist = Safelist.relaxed()
                .addTags("h1", "h2", "code")
                .addAttributes("a", "href", "target")
                .addAttributes(":all", "class")
                .addProtocols("a", "href", "http", "https");

        // Sanitizar el contenido
        String cleanContent = Jsoup.clean(newContent, safelist);

        comment.setContent(cleanContent);
        comment.setModified(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    /**
     * Elimina un comentario, validando que el usuario sea el autor.
     */
    @Transactional
    public boolean deleteComment(Long userId, long soundId, long commentId) {
        Optional<Comment> optComment = commentRepository.findById(commentId);
        if (optComment.isPresent()) {
            Comment comment = optComment.get();
            // Verificar si el usuario es ADMIN
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            if ((isAdmin || comment.getUser().getUserId() == userId)
                    && comment.getSoundId() == soundId) {
                commentRepository.delete(comment);
                return true;
            } else {
                throw new SecurityException("No tienes permiso para eliminar este comentario");
            }
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

    /* Obtiene la lista de comentarios realizados por un usuario. */

    public List<Comment> getCommentsByUserId(long userId) {
        return commentRepository.findByUserUserId(userId);
    }

    // */Borra los comentarios dependiendo del tipo de usuario que seas */

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
     * Obtiene los comentarios de un sonido con imágenes de perfil en Base64 y datos
     * adicionales.
     */
    public List<Map<String, Object>> getCommentsWithImagesBySoundId(long soundId, Long currentUserId) {
        List<Comment> comments = commentRepository.findBySoundId(soundId);
        List<Map<String, Object>> commentsWithImages = new ArrayList<>();

        for (Comment comment : comments) {
            // Determinar si el usuario actual es el propietario del comentario
            boolean owner = (currentUserId != null && currentUserId.equals(comment.getUser().getUserId()));
            comment.setCommentOwner(owner);

            // Obtener información del usuario del comentario
            User commentUser = comment.getUser();

            // Crear un mapa con los datos del comentario (sin Base64)
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("commentId", comment.getCommentId());
            commentData.put("user", commentUser); // Enviamos el User completo para acceder a su ID
            commentData.put("content", comment.getContent());
            commentData.put("created", comment.getCreated());
            commentData.put("isCommentOwner", comment.isCommentOwner());
            commentData.put("hasProfilePicture", commentUser.getProfilePicture() != null); // Usamos el Blob para la
                                                                                           // bandera
            commentData.put("userInitial", userService.getProfileInfo(commentUser).get("userInitial"));
            commentData.put("soundId", soundId);
            commentData.put("username", commentUser.getUsername());

            commentsWithImages.add(commentData);
        }

        return commentsWithImages;
    }

    public Page<Comment> findBySoundId(long soundId, Pageable pageable, HttpSession session) {
        return commentRepository.findBySound_SoundIdOrderByCreatedDesc(soundId, pageable);
    }
}
