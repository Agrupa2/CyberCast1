package es.swapsounds.controller;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.service.CommentService;
import es.swapsounds.service.ProfileService;
import es.swapsounds.service.SoundService;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private SoundService soundService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProfileService profileService;

    @GetMapping("/profile/{username}")
    public String userProfile(@PathVariable("username") String username, HttpSession session, Model model) {
        Optional<User> profileUserOpt = userService.findUserByUsername(username);
        if (!profileUserOpt.isPresent()) {
            return "redirect:/start";
        }
        User profileUser = profileUserOpt.get();

        Long authenticatedUserId = userService.getUserIdFromSession(session);
        boolean isOwner = authenticatedUserId != null && authenticatedUserId.equals(profileUser.getUserId());

        List<Sound> userSounds = soundService.getSoundByUserId(profileUser.getUserId());
        List<Comment> userComments = commentService.getCommentsByUserId(profileUser.getUserId());
        String userInitial = profileService.getUserInitial(profileUser);

        // Convertir Blob a Base64 si existe
        String profileImageBase64 = "";
        Blob profilePicture = profileUser.getProfilePicture();
        if (profilePicture != null) {
            try {
                byte[] imageBytes = profilePicture.getBytes(1, (int) profilePicture.length());
                profileImageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
            } catch (SQLException e) {
                System.err.println("Error al convertir el Blob a Base64: " + e.getMessage());
            }
        }

        // Agregar datos al modelo
        model.addAttribute("comments", userComments);
        model.addAttribute("profileImageBase64", profileImageBase64);
        model.addAttribute("userInitial", userInitial);
        model.addAttribute("username", profileUser.getUsername());
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("sounds", userSounds);
        model.addAttribute("user", profileUser);

        return "profile";
    }

    @PostMapping("/profile/update-username")
    public String updateUsername(@RequestParam String newUsername,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long userId = userService.getUserIdFromSession(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (newUsername == null || newUsername.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario no puede estar vacío");
            return "redirect:/profile";
        }

        userService.updateUsername(userId, newUsername.trim());
        session.setAttribute("username", newUsername.trim());
        redirectAttributes.addFlashAttribute("success", "Nombre de usuario actualizado");
        return "redirect:/profile";
    }

    @PostMapping("/profile/update-avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long userId = userService.getUserIdFromSession(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            userService.updateProfilePicture(userId, file);
            redirectAttributes.addFlashAttribute("success", "Avatar actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String redirectToOwnProfile(HttpSession session) {
        Long userId = userService.getUserIdFromSession(session);
        if (userId == null) {
            return "redirect:/login";
        }
        Optional<User> userOpt = userService.getUserById(userId);
        if (!userOpt.isPresent()) {
            return "redirect:/start";
        }
        User user = userOpt.get();
        return "redirect:/profile/" + user.getUsername();
    }
}
