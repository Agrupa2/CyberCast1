package es.swapsounds.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.CommentRepository;
import es.swapsounds.storage.InMemoryStorage;
import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private InMemoryStorage storage;

    @GetMapping("/profile")
    public String userProfile(HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = storage.findUserById(userId);
        if (!userOpt.isPresent()) {
            return "redirect:/start";
        }

        User user = userOpt.get();

        String userInitial = "?"; // Valor por defecto
            
            // Asignamos profileImagePath desde el usuario
        String profileImagePath = user.getProfilePicturePath();
            
        if (profileImagePath == null) {
            userInitial = user.getUsername().length() > 0
                    ? user.getUsername().substring(0, 1).toUpperCase()
                    : "?";
            }

        
        List<Sound> userSounds = storage.getSoundsByUserId(userId);
        List<Comment> userComments = commentRepository.getCommentsByUserId(userId); // Nuevo: Obtener comentarios

        model.addAttribute("comments", userComments); // Añadir comentarios al modelo
        model.addAttribute("profileImagePath", profileImagePath); 
        model.addAttribute("userInitial", userInitial);
        model.addAttribute("username", username);
        model.addAttribute("user", user);
        model.addAttribute("sounds", userSounds);

        return "profile";
    }


    @PostMapping("/profile/update-username")
    public String updateUsername(
            @RequestParam String newUsername,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null)
            return "redirect:/login";

        if (newUsername == null || newUsername.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario no puede estar vacío");
            return "redirect:/profile";
        }

        storage.updateUsername(userId, newUsername.trim());
        session.setAttribute("username", newUsername.trim());

        redirectAttributes.addFlashAttribute("success", "Nombre de usuario actualizado");
        return "redirect:/profile";
    }

    @PostMapping("/profile/update-avatar")
    public String updateAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null)
            return "redirect:/login";

        try {
            String uploadDir = "uploads/profiles/";
            Files.createDirectories(Paths.get(uploadDir));

            String filename = userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + filename);
            file.transferTo(filePath);

            storage.updateProfilePicture(userId, "/" + uploadDir + filename);

            redirectAttributes.addFlashAttribute("success", "Avatar actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen");
        }

        return "redirect:/profile";
    }
}