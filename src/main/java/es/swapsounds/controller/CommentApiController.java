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
            @PathVariable int soundId, // <-- Usar Long en lugar de int
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Validar usuario
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        // Obtener usuario desde el repositorio (no usar username)
        User currentUser = storage.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear y guardar comentario
        Comment comment = commentRepository.addComment(
                soundId,
                content,
                currentUser // Pasar el objeto User completo
        );

        redirectAttributes.addFlashAttribute("message", "Comentario publicado!");
        return "redirect:/sounds/" + soundId;
    }

    @PostMapping("/sounds/{soundId}/comments/{commentId}/edit")
    public String editComment(
            @PathVariable int soundId,
            @PathVariable String commentId,
            @RequestParam String content,
            HttpSession session) {

        // Validar usuario
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User currentUser = storage.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar comentario
        boolean success = commentRepository.editComment(
                soundId,
                commentId,
                content,
                currentUser // Pasar User completo para validación
        );

        return "redirect:/sounds/" + soundId;
    }

}