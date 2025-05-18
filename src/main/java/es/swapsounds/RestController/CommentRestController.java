package es.swapsounds.RestController;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.swapsounds.DTO.CommentDTO;
import es.swapsounds.DTO.CommentMapper;
import es.swapsounds.model.Comment;
import es.swapsounds.service.CommentService;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/sounds/{soundId}/comments")
public class CommentRestController {

    private final CommentService commentService;
    private final UserService userService;
    private final CommentMapper commentMapper;

    public CommentRestController(CommentService commentService, CommentMapper commentMapper, UserService userService) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
        this.userService = userService;
    }

    /**
     * Returns a paginated list of comments for a sound.
     */
    @GetMapping
    public ResponseEntity<List<CommentDTO>> listComments(
            @PathVariable long soundId,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDTO> dtos = commentService.findBySoundId(soundId, pageable)
                .map(c -> commentMapper.toDto(c));
        return ResponseEntity.ok(dtos.getContent());
    }

    /**
     * Adds a new comment to a sound.
     */
    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable long soundId,
            @RequestParam String content,
            HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        Long userId = userService.getUserIdFromPrincipal(principal);
        Comment comment = commentService.addComment(userId, soundId, content);
        CommentDTO dto = commentMapper.toDto(comment);
        URI location = URI.create(String.format("/api/sounds/%d/comments/%d", soundId, dto.commentId()));
        return ResponseEntity.created(location).body(dto);
    }

    /**
     * Edits an existing comment.
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> editComment(
            @PathVariable long soundId,
            @PathVariable long commentId,
            @RequestParam String content,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        Long userId = userService.getUserIdFromPrincipal(principal);
        Comment comment = commentService.commentEdition(userId, soundId, commentId, content);
        CommentDTO dto = commentMapper.toDto(comment);
        return ResponseEntity.ok(dto);
    }

    /**
     * Deletes a comment.
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable long soundId,
            @PathVariable long commentId,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        Long userId = userService.getUserIdFromPrincipal(principal);
        commentService.deleteComment(userId, soundId, commentId);
        return ResponseEntity.noContent().build();
    }

}
