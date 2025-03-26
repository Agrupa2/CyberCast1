package es.swapsounds.controller;

import org.springframework.stereotype.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryCommentRepository;
import es.swapsounds.storage.InMemoryStorage;
import jakarta.servlet.http.HttpSession;

@Controller

public class CommentApiController {

    @Autowired
    private InMemoryCommentRepository inMemoryCommentRepository;
    @Autowired
    private InMemoryStorage storage;

    @PostMapping("/sounds/{soundId}/comments")
    public String addComment(
            @PathVariable long soundId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Obtener el usuario actual
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return "redirect:/login";

        User currentUser = storage.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener el sonido
        Sound sound = storage.getSoundById(soundId);
        if (sound == null) {
            redirectAttributes.addFlashAttribute("error", "Sonido no encontrado");
            return "redirect:/start";
        }

        // Crear y guardar el comentario
        Comment comment = inMemoryCommentRepository.addComment(
                soundId,
                sound.getTitle(), // Pasar el título del sonido
                content,
                currentUser);

        redirectAttributes.addFlashAttribute("success", "Comentario publicado");
        return "redirect:/sounds/" + soundId;
    }

    @PostMapping("/sounds/{soundId}/comments/{commentId}/edit")
    public String editComment(
            @PathVariable long soundId,
            @PathVariable String commentId,
            @RequestParam String content,
            HttpSession session) {

        // User validation
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User currentUser = storage.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Updating the comment with the user input
        boolean success = inMemoryCommentRepository.editComment(
                soundId,
                commentId,
                content,
                currentUser);

        return "redirect:/sounds/" + soundId;
    }

    @PostMapping("/sounds/{soundId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable long soundId,
            @PathVariable long commentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Validate logged users
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null)
            return "redirect:/login";

        // Search for the comment
        Comment comment = inMemoryCommentRepository.findCommentById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        // ID author and session validation
        if (comment.getAuthorId() != currentUserId) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso");
            return "redirect:/sounds/" + soundId;
        }

        // Deleting comment
        inMemoryCommentRepository.deleteComment(commentId);

        redirectAttributes.addFlashAttribute("success", "Comentario eliminado");
        return "redirect:/sounds/" + soundId;
    }

}