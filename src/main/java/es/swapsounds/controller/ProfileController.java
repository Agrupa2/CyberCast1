package es.swapsounds.controller;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.service.CommentService;
import es.swapsounds.service.SoundService;
import es.swapsounds.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SoundService soundService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/profile/{username}")
    public String userProfile(@PathVariable("username") String username, Principal principal, Model model) {
        Optional<User> profileUserOpt = userService.findUserByUsername(username);
        if (!profileUserOpt.isPresent()) {
            return "redirect:/sounds";
        }

        User profileUser = profileUserOpt.get();

        boolean isOwner = false;
        if (principal != null) {
            String loggedUsername = principal.getName();
            isOwner = loggedUsername.equals(profileUser.getUsername());
            model.addAttribute("loggedUsername", loggedUsername); // por si quieres usarlo en la vista
        }

        List<Sound> userSounds = soundService.getSoundByUserId(profileUser.getUserId());
        List<Comment> userComments = commentService.getCommentsByUserId(profileUser.getUserId());

        Map<String, Object> profileInfo = userService.getProfileInfo(profileUser);
        model.addAttribute("profileImageBase64", profileInfo.get("profileImageBase64"));
        model.addAttribute("userInitial", profileInfo.get("userInitial"));
        model.addAttribute("hasProfilePicture", profileInfo.get("hasProfilePicture"));
        model.addAttribute("comments", userComments);
        model.addAttribute("username", profileUser.getUsername()); // este es el perfil visitado
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("sounds", userSounds);
        model.addAttribute("user", profileUser); // redundante si ya tienes profileUser

        return "profile";
    }

    @PostMapping("/profile/update-username")
    public String updateUsername(@RequestParam String newUsername,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (newUsername == null || newUsername.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario no puede estar vacío");
            return "redirect:/profile";
        }

        Long userId = userService.findUserByUsername(principal.getName())
                .map(User::getUserId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        userService.updateUsername(userId, newUsername.trim());
        // Crear nuevo Authentication con el nuevo nombre
        UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(newUsername.trim());
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                updatedUserDetails.getPassword(),
                updatedUserDetails.getAuthorities());

        // Reemplazar el Authentication actual en el SecurityContext
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        redirectAttributes.addFlashAttribute("success", "Nombre de usuario actualizado");
        return "redirect:/profile";
    }

    @PostMapping("/profile/update-avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Long userId = userService.findUserByUsername(principal.getName())
                .map(User::getUserId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        try {
            userService.updateProfilePicture(userId, file);
            redirectAttributes.addFlashAttribute("success", "Avatar actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String redirectToOwnProfile(Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        Optional<User> userOpt = userService.findUserByUsername(username);
        if (!userOpt.isPresent()) {
            return "redirect:/sounds";
        }
        User user = userOpt.get();
        return "redirect:/profile/" + user.getUsername();
    }
}
