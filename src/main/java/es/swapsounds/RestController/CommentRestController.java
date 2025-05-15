package es.swapsounds.RestController;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.swapsounds.dto.CommentDTO;
import es.swapsounds.dto.CommentMapper;
import es.swapsounds.model.Comment;
import es.swapsounds.service.CommentService;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpSession;

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

    @GetMapping
    public ResponseEntity<List<CommentDTO>> listComments(
            @PathVariable long soundId,
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDTO> dtos = commentService.findBySoundId(soundId, pageable, session)
                .map(c -> commentMapper.toDto(c));
        return ResponseEntity.ok(dtos.getContent());
    }

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable long soundId,
            @RequestParam String content,
            HttpSession session) {
        long userId = userService.getUserIdFromSession(session);
        Comment comment = commentService.addComment(userId, soundId, content);
        CommentDTO dto = commentMapper.toDto(comment);
        URI location = URI.create(String.format("/api/sounds/%d/comments/%d", soundId, dto.commentId()));
        return ResponseEntity.created(location).body(dto);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> editComment(
            @PathVariable long soundId,
            @PathVariable long commentId,
            @RequestParam String content,
            HttpSession session) {
        Long userId = userService.getUserIdFromSession(session);
        Comment comment = commentService.commentEdition(userId, soundId, commentId, content);
        CommentDTO dto = commentMapper.toDto(comment);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable long soundId,
            @PathVariable long commentId,
            HttpSession session) {
        Long userId = userService.getUserIdFromSession(session);
        commentService.deleteComment(userId, soundId, commentId);
        return ResponseEntity.noContent().build();
    }

}
