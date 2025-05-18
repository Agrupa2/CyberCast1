package es.swapsounds.service;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.CommentRepository;
import jakarta.transaction.Transactional;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
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
     * Adds a comment to a sound, validating the user and sanitizing the content.
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

        // Configurate the list of allowed tags and attributes
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String cleanContent = policy.sanitize(content);

        // Get the user
        User currentUser = userService.findUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Get the sound
        Sound sound = soundService.findSoundById(soundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sonido no encontrado"));

        String soundTitle = sound.getTitle();

        // Create the comment
        Comment comment = new Comment();
        comment.setContent(cleanContent);
        comment.setUser(currentUser);
        comment.setSound(sound);
        comment.setSoundId(soundId);
        comment.setSoundTitle(soundTitle);
        comment.setCreated(LocalDateTime.now());

        // Store the comment
        return commentRepository.save(comment);
    }

    /**
     * Edit a comment, validating the user and sanitizing the content.
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

                PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
                String cleanContent = policy.sanitize(newContent);

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

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (comment.getUser().getUserId() == userId || isAdmin) {
            Safelist safelist = Safelist.relaxed()
                    .addTags("h1", "h2", "code")
                    .addAttributes("a", "href", "target")
                    .addAttributes(":all", "class")
                    .addProtocols("a", "href", "http", "https");

            // Sanitized content
            String cleanContent = Jsoup.clean(newContent, safelist);

            comment.setContent(cleanContent);
            comment.setModified(LocalDateTime.now());

        } else {
            throw new SecurityException("No autorizado para editar este comentario");
        }

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
            // Verify if the user is the owner of the comment or an admin
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
     * Look for a comment by its ID.
     */

    public Optional<Comment> findCommentById(long commentId) {
        return commentRepository.findById(commentId);
    }

    /**
     * Get the comment's list associated to a sound.
     */
    public List<Comment> getCommentsBySoundId(long soundId) {
        return commentRepository.findBySoundId(soundId);
    }

    /* Get the comment's list by a user */

    public List<Comment> getCommentsByUserId(long userId) {
        return commentRepository.findByUserUserId(userId);
    }

    // */Delete comments */

    @Transactional
    public void deleteCommentsByUserId(long userId) {
        commentRepository.deleteByUserUserId(userId);
    }

    // Delete comments by soundId

    @Transactional
    public void deleteCommentsBySoundId(long soundId) {
        commentRepository.deleteBySoundId(soundId);
    }

    /**
     * Get the comment's list associated to a sound with user information and image
     */
    public List<Map<String, Object>> getCommentsWithImagesBySoundId(long soundId, Long currentUserId) {
        List<Comment> comments = commentRepository.findBySoundId(soundId);
        List<Map<String, Object>> commentsWithImages = new ArrayList<>();

        for (Comment comment : comments) {
            // Determinate if the current user is the owner of the comment
            boolean owner = (currentUserId != null && currentUserId.equals(comment.getUser().getUserId()));
            comment.setCommentOwner(owner);

            // Get the user who made the comment
            User commentUser = comment.getUser();

            // Create a map to store the comment data
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

    public Page<Comment> findBySoundId(long soundId, Pageable pageable) {
        return commentRepository.findBySound_SoundIdOrderByCreatedDesc(soundId, pageable);
    }

    public boolean canEditComment(Comment comment, Long userId, boolean isAdmin) {
        return isAdmin || (userId != null && userId.equals(comment.getUser().getUserId()));
    }

    public List<Map<String, Object>> getCommentsWithImagesAndPermissions(long soundId, Long userId, boolean isAdmin) {
        List<Map<String, Object>> commentsWithImages = getCommentsWithImagesBySoundId(soundId, userId);
        for (Map<String, Object> comment : commentsWithImages) {
            Long commentUserId = (Long) comment.get("userId");
            boolean canEditComment = (userId != null && userId.equals(commentUserId)) || isAdmin;
            comment.put("canEditComment", canEditComment);
        }
        return commentsWithImages;
    }
}
