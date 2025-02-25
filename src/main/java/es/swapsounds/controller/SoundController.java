package es.swapsounds.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryStorage;
import jakarta.servlet.http.HttpSession;

@Controller
public class SoundController {

    @Autowired
    private InMemoryStorage storage;

    
    @GetMapping("/start")
    public String showSounds(Model model, HttpSession session) {
        // Obtener datos de la sesión
        String username = (String) session.getAttribute("username");
        Integer userId = (Integer) session.getAttribute("userId");

        if (username != null && userId != null) {
            model.addAttribute("message", "Welcome, " + username + "!");
            model.addAttribute("username", username);
            model.addAttribute("userId", userId);
        }

        List<Sound> sounds = storage.getAllSounds();
        model.addAttribute("sounds", sounds != null ? sounds : new ArrayList<>());

        return "start";
    }

    @GetMapping("/sounds/upload")
    public String showUploadForm(@RequestParam(required = false) String username, Model model) {
    System.out.println("Accediendo a /sounds/upload con username: " + username);
    if (username == null) {
        model.addAttribute("error", "You must be logged in to upload sounds.");
        return "login";
    }

    Optional<User> user = storage.findUserByUsername(username);
    if (user.isPresent()) {
        model.addAttribute("userId", user.get().getUserId());
        model.addAttribute("username", username);
        System.out.println("Usuario encontrado: " + username + ", userId: " + user.get().getUserId());
        return "upload-sound";
    } else {
        System.out.println("Usuario no encontrado para username: " + username);
        model.addAttribute("error", "User not found. Please login again.");
        return "login";
    }
}

@PostMapping("/sounds/upload")
public String uploadSound(
        @RequestParam String title,
        @RequestParam String description,
        @RequestParam String category,
        @RequestParam String duration,
        @RequestParam MultipartFile audioFile,
        @RequestParam MultipartFile imageFile,
        HttpSession session, // Usar la sesión para obtener el userId
        Model model) throws IOException {

    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        model.addAttribute("error", "You must be logged in to upload sounds.");
        return "redirect:/login"; // Redirigir al login si no hay sesión
    }

    Optional<User> user = storage.findUserById(userId);
    if (!user.isPresent()) {
        model.addAttribute("error", "User not found. Please login again.");
        session.invalidate(); // Eliminar la sesión corrupta
        return "redirect:/login";
    }

    // Guarda los archivos usando el nombre de usuario
    String username = user.get().getUsername();
    String audioPath = storage.saveFile(username, audioFile, "sounds");
    String imagePath = storage.saveFile(username, imageFile, "images");

    // Crea y guarda el sonido
    Sound sound = new Sound(0, title, description, audioPath, imagePath, userId, category, duration);
    storage.addSound(sound);

    model.addAttribute("success", "Sound uploaded successfully!");
    model.addAttribute("username", username);
    model.addAttribute("userId", userId);
    return "upload-sound";
}
}