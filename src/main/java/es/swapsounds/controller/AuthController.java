package es.swapsounds.controller;

import es.swapsounds.model.User;
import es.swapsounds.service.AuthService;
import es.swapsounds.service.UserService; // Import UserService
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService; // Inject UserService instead of AuthService

    @Autowired
    private AuthService authService;

    @GetMapping("/signup")
    public String showRegisterForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String user_password,
            @RequestParam(required = false) MultipartFile profile_photo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User user = new User(user_password, user_password, user_password, null); // Crear un nuevo objeto User y
                                                                                     // setear los valores
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(user_password);

            Blob defaultProfilePictureBlob = null;
            try (InputStream defaultImageStream = getClass()
                    .getResourceAsStream("/static/images/UserDefaultPhoto.jpg")) {
                if (defaultImageStream != null) {
                    defaultProfilePictureBlob = new SerialBlob(defaultImageStream.readAllBytes());
                } else {
                    System.err.println("¡Error al cargar la imagen de perfil por defecto!");
                }
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                System.err.println("Error al convertir la imagen de perfil por defecto a Blob: " + e.getMessage());
            }

            user.setProfilePicture(defaultProfilePictureBlob);
            userService.addUser(user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            redirectAttributes.addFlashAttribute("success", "¡Registro exitoso!");
            return "redirect:/start";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String user_password,
            HttpSession session,
            Model model) {

        Optional<User> user = authService.authenticate(username, user_password);
        if (user.isPresent()) {
            User actualUser = user.get();
            session.setAttribute("username", actualUser.getUsername());
            session.setAttribute("userId", actualUser.getUserId());
            return "redirect:/start";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}