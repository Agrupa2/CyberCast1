package es.swapsounds.controller;

import org.springframework.stereotype.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.swapsounds.model.Comment;

import es.swapsounds.model.User;
import es.swapsounds.storage.CommentRepository;
import es.swapsounds.storage.InMemoryStorage;
import jakarta.servlet.http.HttpSession;

@Controller

public class CommentApiController {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private InMemoryStorage storage;

    @PostMapping("/sounds/{soundId}/comments")
    public String addComment(
            @PathVariable int soundId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Logged user validation
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        // Obtaining the user form InMemoryStorage
        User currentUser = storage.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Creating comment
        Comment comment = commentRepository.addComment(
                soundId,
                content,
                currentUser);

        redirectAttributes.addFlashAttribute("message", "Comentario publicado!");
        return "redirect:/sounds/" + soundId;
    }

    @PostMapping("/sounds/{soundId}/comments/{commentId}/edit")
    public String editComment(
            @PathVariable int soundId,
            @PathVariable String commentId,
            @RequestParam String content,
            HttpSession session) {

        // User validation
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User currentUser = storage.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Updating the comment with the user input
        boolean success = commentRepository.editComment(
                soundId,
                commentId,
                content,
                currentUser);

        return "redirect:/sounds/" + soundId;
    }

    @PostMapping("/sounds/{soundId}/comments/{commentId}/delete")
public String deleteComment(
        @PathVariable int soundId,
        @PathVariable String commentId,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

    // 1. Validar usuario logueado
    Integer currentUserId = (Integer) session.getAttribute("userId");
    if (currentUserId == null) return "redirect:/login";

    // 2. Buscar el comentario
    Comment comment = commentRepository.findCommentById(commentId)
        .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

    // 3. Validar propiedad (ID del autor vs ID de sesión)
    if (comment.getAuthorId() != currentUserId) {
        redirectAttributes.addFlashAttribute("error", "No tienes permiso");
        return "redirect:/sounds/" + soundId;
    }

    // 4. Eliminar
    commentRepository.deleteComment(commentId);
    
    redirectAttributes.addFlashAttribute("success", "Comentario eliminado");
    return "redirect:/sounds/" + soundId;
}

}