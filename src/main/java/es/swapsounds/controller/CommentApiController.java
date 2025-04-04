package es.swapsounds.controller;

import es.swapsounds.service.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentApiController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/sounds/{soundId}/comments")
    public String addComment(
            @PathVariable long soundId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return "redirect:/login";

        try {
            commentService.addComment(userId, soundId, content);
            redirectAttributes.addFlashAttribute("success", "Comentario publicado");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sounds/" + soundId;
    }

    @PostMapping("/sounds/{soundId}/comments/{commentId}/edit")
    public String editComment(
            @PathVariable long soundId,
            @PathVariable long commentId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return "redirect:/login";

        try {
            boolean success = commentService.editComment(userId, soundId, commentId, content);
            if (!success) {
                redirectAttributes.addFlashAttribute("error", "No se pudo editar el comentario");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/sounds/" + soundId;
    }

    @PostMapping("/sounds/{soundId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable long soundId,
            @PathVariable long commentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return "redirect:/login";

        try {
            commentService.deleteComment(userId, soundId, commentId);
            redirectAttributes.addFlashAttribute("success", "Comentario eliminado");
        } catch (SecurityException se) {
            redirectAttributes.addFlashAttribute("error", se.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/sounds/" + soundId;
    }
}
