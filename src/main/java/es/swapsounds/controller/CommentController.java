package es.swapsounds.controller;

import es.swapsounds.service.CommentService;
import es.swapsounds.service.UserService;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @PostMapping("/sounds/{soundId}/comments")
    public String addComment(
            @PathVariable long soundId,
            @RequestParam String content,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null)
            return "redirect:/login";

        try {
            Long userId = userService.findUserByUsername(principal.getName()).get().getUserId();
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
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null)
            return "redirect:/login";

        try {
            Long userId = userService.findUserByUsername(principal.getName()).get().getUserId();
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
            Principal principal,
            RedirectAttributes redirectAttributes) {

        
        if (principal == null)
            return "redirect:/login";
                
        try {
            Long userId = userService.findUserByUsername(principal.getName()).get().getUserId();
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
