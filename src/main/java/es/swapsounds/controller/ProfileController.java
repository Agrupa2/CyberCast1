package es.swapsounds.controller;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.service.CommentService;
import es.swapsounds.service.SoundService;
import es.swapsounds.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
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
        boolean isAdmin = false;
        if (principal != null) {
            String loggedUsername = principal.getName();
            isOwner = loggedUsername.equals(profileUser.getUsername());

            // Verificar si el usuario actual es ADMIN
            Optional<User> currentUserOpt = userService.findUserByUsername(loggedUsername);
            if (currentUserOpt.isPresent()) {
                isAdmin = SecurityContextHolder.getContext().getAuthentication()
                        .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        }

        List<Sound> userSounds = soundService.getSoundByUserId(profileUser.getUserId());
        List<Comment> userComments = commentService.getCommentsByUserId(profileUser.getUserId());

        Map<String, Object> profileInfo = userService.getProfileInfo(profileUser);
        model.addAttribute("userInitial", profileInfo.get("userInitial"));
        model.addAttribute("hasProfilePicture", profileInfo.get("hasProfilePicture"));
        model.addAttribute("comments", userComments);
        model.addAttribute("username", profileUser.getUsername()); // este es el perfil visitado
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("sounds", userSounds);
        model.addAttribute("user", profileUser); // redundante si ya tienes profileUser
        model.addAttribute("isAllowedToEdit", isOwner || isAdmin); // Añadir esta línea
        model.addAttribute("isAdmin", isAdmin); // Opcional: si necesitas usar isAdmin en la vista

        return "profile";
    }

    @PostMapping("/profile/update-username")
    public String updateUsername(
            @RequestParam String newUsername,
            @RequestParam Long targetUserId, // ID del usuario a editar
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null)
            return "redirect:/login";

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Obtener usuario actual
        User currentUser = userService.findUserByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Verificar permisos: admin o dueño del perfil
        boolean isAllowed = currentUser.getUserId() == targetUserId
                || isAdmin;

        if (!isAllowed) {
            redirectAttributes.addFlashAttribute("error", "No autorizado");
            return "redirect:/sounds";
        }

        // Actualizar nombre de usuario
        try {
            userService.updateUsername(targetUserId, newUsername.trim());
            redirectAttributes.addFlashAttribute("success", "Nombre actualizado");

            // Actualizar SecurityContext si el usuario editó su propio nombre
            if (currentUser.getUserId() == targetUserId) {
                UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(newUsername.trim());
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                        updatedUserDetails, updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        // Redirigir al perfil actualizado
        String newProfileUsername = userService.findUserById(targetUserId)
                .map(User::getUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return "redirect:/profile/" + newProfileUsername;
    }

    @PostMapping("/profile/update-avatar")
    public String updateAvatar(
            @RequestParam("avatar") MultipartFile file,
            @RequestParam Long targetUserId, // ID del usuario a editar
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null)
            return "redirect:/login";

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Obtener usuario actual
        User currentUser = userService.findUserByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Verificar permisos: admin o dueño del perfil
        boolean isAllowed = currentUser.getUserId() == targetUserId
                || isAdmin;

        if (!isAllowed) {
            redirectAttributes.addFlashAttribute("error", "No autorizado");
            return "redirect:/sounds";
        }

        Long sessionUserId = currentUser.getUserId();

        // Actualizar avatar
        try {
            userService.updateProfilePicture(sessionUserId, targetUserId, file);
            redirectAttributes.addFlashAttribute("success", "Avatar actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        // Redirigir al perfil del usuario editado
        String targetUsername = userService.findUserById(targetUserId)
                .map(User::getUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return "redirect:/profile/" + targetUsername;
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

    @GetMapping("/users/{id}/avatar")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable Long id) {
        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                Blob picBlob = user.getProfilePicture();
                if (picBlob != null) {
                    try (InputStream in = picBlob.getBinaryStream()) {
                        byte[] img = in.readAllBytes();
                        return ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_JPEG) // o MediaType.IMAGE_PNG si fuera PNG
                                .body(img);
                    }
                }
            } catch (SQLException | IOException e) {
                // Loguea el error si quieres
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }

}
