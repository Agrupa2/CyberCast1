package es.swapsounds.controller;

import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryStorage;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private InMemoryStorage storage;

    @GetMapping("/signup")
    public String showRegisterForm(Model model) {
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String user_password,
            @RequestParam(required = false) MultipartFile profile_photo,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws IOException {

        // Validates if the username already exists
        if (storage.findUserByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
            return "redirect:/signup";
        }

        // Check if the user uploaded a profile photo
        String photoPath = null;
        if (profile_photo != null && !profile_photo.isEmpty()) {
            try {
                photoPath = storage.saveFile(username, profile_photo, "profiles");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al subir la imagen de perfil");
                return "redirect:/signup";
            }
        } else {
            // Asign default profile photo
            photoPath = "/uploads/profiles/default-avatar.png";
        }

        User user = new User(username, email, user_password, photoPath);

        storage.addUser(user);

        // Autologin después del registro
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", username);

        if (user_password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
            return "redirect:/signup";
        }

        redirectAttributes.addFlashAttribute("success", "¡Registro exitoso!");
        return "redirect:/start";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String user_password,
            HttpSession session, // Adding HttpSession as a parameter
            Model model) {

        Optional<User> user = storage.authenticate(username, user_password);
        if (user.isPresent()) {
            // Obtain the user's username and userId
            session.setAttribute("username", user.get().getUsername());
            session.setAttribute("userId", user.get().getUserId());

            // Redirect tp start after successful login
            return "redirect:/start";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Deleting the user session with it's session data
        return "redirect:/login";
    }
}