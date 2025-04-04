package es.swapsounds.controller;

import es.swapsounds.model.User;
import es.swapsounds.service.AuthService;
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

@Controller
public class AuthController {

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
            User user = authService.registerUser(username, email, user_password, profile_photo);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            redirectAttributes.addFlashAttribute("success", "¡Registro exitoso!");
            return "redirect:/start";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen de perfil");
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

        User user = authService.authenticate(username, user_password);
        if (user != null) {
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userId", user.getUserId());
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
