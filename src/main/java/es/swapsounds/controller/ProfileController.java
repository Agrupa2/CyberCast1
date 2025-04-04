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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/profile")
    public String userProfile(HttpSession session, Model model) {
        // Delegamos la obtención del ID de usuario a UserService
        Long userId = userService.getUserIdFromSession(session);
        if (userId == null) {
            return "redirect:/login";
        }

        // Obtener el usuario y validar su existencia
        Optional<User> userOpt = userService.getUserById(userId);
        if (!userOpt.isPresent()) {
            return "redirect:/start";
        }
        User user = userOpt.get();

        // Obtener inicial del usuario (en caso de no tener avatar)
        String userInitial = profileService.getUserInitial(user);

        // Obtener sonidos y comentarios del usuario usando SoundService y CommentService
        List<Sound> userSounds = soundService.getSoundByUserId(userId);
        List<Comment> userComments = commentService.getCommentsByUserId(userId);

        // Agregar atributos al modelo
        model.addAttribute("comments", userComments);
        model.addAttribute("profileImagePath", user.getProfilePicturePath());
        model.addAttribute("userInitial", userInitial);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("sounds", userSounds);

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
            profileService.updateProfilePicture(userId, file);
            redirectAttributes.addFlashAttribute("success", "Avatar actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}
