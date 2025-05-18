package es.swapsounds.controller;

import es.swapsounds.DTO.AdminUserViewDTO;
import es.swapsounds.model.User;
import es.swapsounds.service.CommentService;
import es.swapsounds.service.SoundService;
import es.swapsounds.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // All routes in /admin only for ROLE_ADMIN
public class AdminController {

    private final UserService userService;

    private final CommentService commentService;

    private final SoundService soundService;

    public AdminController(UserService userService, CommentService commentService, SoundService soundService) {
        this.userService = userService;
        this.commentService = commentService;
        this.soundService = soundService;
    }

    /** List all users **/
    @GetMapping("/users")
    public String listUsers(Principal principal, Model model) {
        if (principal == null) {
            // 401 if not authenticated
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in");
        }
        // If not ADMIN, 403 Forbidden
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access");
        }

        // If admin, render the list
        model.addAttribute("username", principal.getName());
        List<User> users = userService.getAllUsers();
        List<AdminUserViewDTO> views = users.stream()
                .map(AdminUserViewDTO::new)
                .toList();

        model.addAttribute("users", views);
        model.addAttribute("sounds", soundService.getAllSounds());
        return "admin-users";
    }

    /** Delete a user by ID **/
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
            Principal principal,
            Model model) {
        // Admin cannot delete their own account
        if (userService.findUserByUsername(principal.getName())
                .map(u -> u.getUserId() == id)
                .orElse(false)) {
            model.addAttribute("error", "You cannot delete your own account");
            return "redirect:/admin/users";
        }

        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    // Delete sounds as admin
    @PostMapping("/sounds/{id}/delete")
    public String deleteSound(@PathVariable Long id,
            Principal principal,
            Model model) {

        commentService.deleteCommentsBySoundId(id);
        soundService.deleteSound(id);
        model.addAttribute("success", "The sound has been deleted successfully.");
        return "redirect:/admin/users";
    }
}
