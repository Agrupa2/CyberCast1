package es.swapsounds.controller;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private InMemoryStorage storage;

    @GetMapping("/signup")
    public String showRegisterForm(Model model) {
        return "signup"; // Plantilla register.mustache
    }

    @PostMapping("/signup")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String user_password,
            @RequestParam(required = false) MultipartFile profile_photo,
            Model model) throws IOException {

        Optional<User> existingUser = storage.findUserByUsername(username);
        if (existingUser.isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "login";
        }

        String photoPath = null;
        if (profile_photo != null && !profile_photo.isEmpty()) {
            photoPath = storage.saveProfilePhoto(username, profile_photo.getOriginalFilename());
            profile_photo.transferTo(new java.io.File(photoPath));
        }

        User user = new User( username, email, user_password, photoPath, 0, photoPath);
        storage.addUser(user);
        model.addAttribute("success", "User registered successfully! Please login.");
        return "login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Plantilla login.mustache
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String user_password, Model model) {
        Optional<User> user = storage.authenticate(username, user_password);
        if (user.isPresent()) {
            model.addAttribute("message", "Login successful! Welcome, " + user.get().getUsername() + "!");
            model.addAttribute("username", user.get().getUsername()); // Para pasar el username a start.mustache
            model.addAttribute("userId", user.get().getUserId()); // Pasar el userId para verificar en SoundController
            List<Sound> allSounds = storage.getAllSounds();
            model.addAttribute("sounds", allSounds != null ? allSounds : new ArrayList<>());
            return "/start"; // Redirige a la página de sonidos después del login
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }
}