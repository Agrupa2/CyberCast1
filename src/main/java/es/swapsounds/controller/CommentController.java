package es.swapsounds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import es.swapsounds.storage.CommentRepository;
import jakarta.servlet.http.HttpSession;

import java.util.Map;
import es.swapsounds.dto.CommentRequest;

import es.swapsounds.model.Comment;
import es.swapsounds.model.User;


@RestController
@RequestMapping("/sounds")
public class CommentController {
    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) { //pilla los comentarios de los sonidos
        this.commentRepository = commentRepository;
    }

    @Autowired
    private HttpSession session; // Inyectar la sesión HTTP

    // Añadir comentario (usuario autenticado)
    @PostMapping("/{soundId}/comments")
    public ResponseEntity<Comment> addComment(
        @PathVariable Long soundId,
        @RequestBody CommentRequest request
        //Authentication authentication // Usuario autenticado

    ) {
        /*String username = authentication.getName(); // Obtener nombre del usuario*/
        //User username = (User) authentication.getPrincipal(); //Debería obtener el usuario autenticado
        User username = getCurrentUser();
        Comment comment = commentRepository.addComment(
            soundId,
            request.getContent(),
            username // Autor obtenido de la autenticación
        );
        return ResponseEntity.ok(comment);
    }

    // Editar comentario (solo autor)
    @PutMapping("/{soundId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(
        @PathVariable Long soundId,
        @PathVariable String commentId,
        @RequestBody Map<String, String> request
        //Authentication authentication
    ) {
        //String username = authentication.getName();
        //User username = (User) authentication.getPrincipal();
        User username = getCurrentUser(); 
        boolean success = commentRepository.editComment(
            soundId,
            commentId,
            request.get("content"),
            username // Validación automática del autor
        );

        return success ?
            ResponseEntity.ok().build() :
            ResponseEntity.status(403).body("No tienes permisos");
    }

    // Eliminar comentario (solo autor)
    @DeleteMapping("/{soundId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
        @PathVariable Long soundId,
        @PathVariable String commentId
        //Authentication authentication
    ) {
        //String username = authentication.getName();
        //User username = (User) authentication.getPrincipal();
        User username = getCurrentUser(); // Método para obtener el usuario actual;
        boolean success = commentRepository.deleteComment(
            soundId,
            commentId,
            username
        );

        return success ?
            ResponseEntity.ok().build() :
            ResponseEntity.status(403).body("No tienes permisos");
    }

    private User getCurrentUser() {
        return (User) session.getAttribute("user"); // Recuperar el usuario de la sesión
    }
}

// DTO sin campo "author" (el usuario viene de la autenticación)
/*class CommentRequest {
    private String content;

    // Getters y Setters
}*/ 