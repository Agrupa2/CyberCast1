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

            // Check if the current user is ADMIN
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
        model.addAttribute("username", profileUser.getUsername()); // this is the visited profile
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("sounds", userSounds);
        model.addAttribute("user", profileUser); // redundant if you already have profileUser
        model.addAttribute("isAllowedToEdit", isOwner || isAdmin); // Add this line
        model.addAttribute("isAdmin", isAdmin); // Optional: if you need to use isAdmin in the view

        return "profile";
    }

    @PostMapping("/profile/update-username")
    public String updateUsername(
            @RequestParam String newUsername,
            @RequestParam Long targetUserId, // ID of the user to edit
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null)
            return "redirect:/login";

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Get current user
        User currentUser = userService.findUserByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Check permissions: admin or profile owner
        boolean isAllowed = currentUser.getUserId() == targetUserId
                || isAdmin;

        if (!isAllowed) {
            redirectAttributes.addFlashAttribute("error", "Not authorized");
            return "redirect:/sounds";
        }

        // Update username
        try {
            userService.updateUsername(targetUserId, newUsername.trim());
            redirectAttributes.addFlashAttribute("success", "Username updated");

            // Update SecurityContext if the user edited their own username
            if (currentUser.getUserId() == targetUserId) {
                UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(newUsername.trim());
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                        updatedUserDetails, updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        // Redirect to the updated profile
        String newProfileUsername = userService.findUserById(targetUserId)
                .map(User::getUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return "redirect:/profile/" + newProfileUsername;
    }

    @PostMapping("/profile/update-avatar")
    public String updateAvatar(
            @RequestParam("avatar") MultipartFile file,
            @RequestParam Long targetUserId, // ID of the user to edit
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null)
            return "redirect:/login";

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Get current user
        User currentUser = userService.findUserByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Check permissions: admin or profile owner
        boolean isAllowed = currentUser.getUserId() == targetUserId
                || isAdmin;

        if (!isAllowed) {
            redirectAttributes.addFlashAttribute("error", "Not authorized");
            return "redirect:/sounds";
        }

        Long sessionUserId = currentUser.getUserId();

        // Update avatar
        try {
            userService.updateProfilePicture(sessionUserId, targetUserId, file);
            redirectAttributes.addFlashAttribute("success", "Avatar updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        // Redirect to the edited user's profile
        String targetUsername = userService.findUserById(targetUserId)
                .map(User::getUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
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
                                .contentType(MediaType.IMAGE_JPEG) // or MediaType.IMAGE_PNG if it is PNG
                                .body(img);
                    }
                }
            } catch (SQLException | IOException e) {
                // Log the error if you want
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }

}
