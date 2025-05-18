package es.swapsounds.controller;

import es.swapsounds.model.User;
import es.swapsounds.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            // 1) registrar
            User user = authService.registerUser(username, email, user_password, profile_photo);

            redirectAttributes.addFlashAttribute("success", "¡Registro exitoso! Por favor inicia sesión");
            return "redirect:/login";

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

    @GetMapping("/logout")
    public String logout(
            Principal principal,
            HttpServletRequest request,
            HttpServletResponse response) {

        new SecurityContextLogoutHandler().logout(request, response, null);
        return "redirect:/login";
    }

}