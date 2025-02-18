package es.swapsounds.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.swapsounds.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;


@Controller
@RequestMapping("/auth") // Rutas base para autenticación
public class AuthController {

    private static final List<User> users = new ArrayList<>();

    @PostMapping("/signup")
    public String signUp(@RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam("profilePicture") MultipartFile profilePicture,
                         RedirectAttributes redirectAttributes) {

        String profilePicBase64 = null;
        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                profilePicBase64 = Base64.getEncoder().encodeToString(profilePicture.getBytes());
            } catch (IOException e) {
                return "Error al procesar la imagen";
            }
        }

        
        // Crear usuario y almacenarlo en memoria
        User newUser = new User(username, email, password, profilePicBase64);
        users.add(newUser);

        // Mensaje de éxito
        redirectAttributes.addFlashAttribute("success", "Usuario registrado. Inicia sesión.");

        // Redirigir a /login
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        RedirectAttributes redirectAttributes) {
        
        // Verificar usuario
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                redirectAttributes.addFlashAttribute("success", "Inicio de sesión exitoso.");
                return "redirect:/home"; // Redirigir a la página principal
            }
        }

        // Si falla el login
        redirectAttributes.addFlashAttribute("error", "Credenciales incorrectas.");
        return "redirect:/login";
    }
}
