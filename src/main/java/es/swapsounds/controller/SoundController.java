package es.swapsounds.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import es.swapsounds.service.SoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.service.UserService;
import es.swapsounds.service.CategoryService;
import es.swapsounds.service.CommentService;
import jakarta.servlet.http.HttpSession;

@Controller
public class SoundController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SoundService soundService;

    @Autowired
    private UserService userService;

    @GetMapping("/sounds")
    public String showSounds(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", defaultValue = "all") String category,
            HttpSession session,
            Model model) {

        // Obtener usuario desde el UserService
        Optional<User> userOpt = userService.getUserFromSession(session);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userId", user.getUserId());
        }

        // Obtener sonidos filtrados usando el SoundService
        List<Sound> filteredSounds = soundService.getFilteredSounds(query, category);
        model.addAttribute("sounds", filteredSounds);
        model.addAttribute("query", query);
        model.addAttribute("category", category);

        // Obtener categorías para el dropdown
        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("allCategories", allCategories);

        model.addAttribute("selectedAll", "all".equalsIgnoreCase(category));
        for (Category cat : allCategories) {
            model.addAttribute("selected" + cat.getName(), category.equalsIgnoreCase(cat.getName()));
        }

        return "sounds";
    }

    @GetMapping("/sounds/upload")
    public String showUploadForm(HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        System.out.println("Accediendo a /sounds/upload con username: " + username);
        if (username == null) {
            model.addAttribute("error", "You must be logged in to upload sounds.");
            return "login";
        }

        // Obtener todas las categorías existentes
        List<Category> allCategories = categoryService.getAllCategories();

        // Añadir al modelo
        model.addAttribute("allCategories", allCategories);

        Optional<User> user = userService.findUserByUsername(username);
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
            @RequestParam List<String> categories,
            @RequestParam MultipartFile audioFile,
            @RequestParam MultipartFile imageFile,
            HttpSession session,
            Model model) throws IOException {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            model.addAttribute("error", "Debes iniciar sesión para subir sonidos.");
            return "redirect:/login";
        }

        Optional<User> user = userService.findUserById(userId);
        User uploader = user.get();
        if (!user.isPresent()) {
            model.addAttribute("error", "Usuario no encontrado.");
            session.invalidate();
            return "redirect:/login";
        }

        soundService.createSound(title, description, categories, audioFile, imageFile, uploader);

        model.addAttribute("success", "¡Sonido subido con éxito!");
        return "redirect:/sounds/" + soundService.getLastInsertedSoundId();
    }

    @GetMapping("/sounds/download")
    public String downloadSounds(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", defaultValue = "all") String category,
            HttpSession session,
            Model model) {

        // Obtener usuario desde el UserService
        Optional<User> userOpt = userService.getUserFromSession(session);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userId", user.getUserId());
        }

        // Obtener sonidos filtrados usando el SoundService
        List<Sound> filteredSounds = soundService.getFilteredSounds(query, category);
        model.addAttribute("sounds", filteredSounds);
        model.addAttribute("query", query);
        model.addAttribute("category", category);

        // Obtener categorías para el dropdown
        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("allCategories", allCategories);

        model.addAttribute("selectedAll", "all".equalsIgnoreCase(category));
        for (Category cat : allCategories) {
            model.addAttribute("selected" + cat.getName(), category.equalsIgnoreCase(cat.getName()));
        }

        return "download-sound";
    }

    @GetMapping("/sounds/{soundId}")
    public String soundDetails(@PathVariable long soundId, HttpSession session, Model model) {
        Long currentUserId = userService.getUserIdFromSession(session);
        String username = (String) session.getAttribute("username");

        Optional<Sound> soundOpt = soundService.findSoundById(soundId);
        if (soundOpt.isEmpty()) {
            return "redirect:/sounds";
        }
        Sound sound = soundOpt.get();
        model.addAttribute("sound", sound);

        Optional<User> uploaderOpt = userService.findUserById(sound.getUserId());
        if (uploaderOpt.isPresent()) {
            User uploader = uploaderOpt.get();
            Map<String, Object> profileInfo = userService.getProfileInfo(uploader);
            model.addAttribute("uploader", uploader);
            model.addAttribute("profileImageBase64", profileInfo.get("profileImageBase64"));
            model.addAttribute("userInitial", profileInfo.get("userInitial"));
        } else {
            model.addAttribute("uploader", null);
        }

        // Obtener comentarios con imágenes procesadas desde CommentService
        List<Map<String, Object>> commentsWithImages = commentService.getCommentsWithImagesBySoundId(soundId,
                currentUserId);
        model.addAttribute("comments", commentsWithImages);

        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("allCategories", allCategories);
        Set<String> selectedCategories = soundService.getSelectedCategoryNames(sound);
        model.addAttribute("selectedCategories", selectedCategories);

        model.addAttribute("isOwner", currentUserId != null && currentUserId.equals(sound.getUserId()));
        model.addAttribute("username", username);

        return "sound-details";
    }

    @PostMapping("/sounds/{soundId}/edit")
    public String editSound(
            @PathVariable long soundId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Set<String> categories,
            @RequestParam(required = false) MultipartFile audioFile,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpSession session,
            Model model) throws IOException {

        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");

        Optional<Sound> originalSound = soundService.findSoundById(soundId);

        // Validar acceso
        if (userId == null || originalSound.isEmpty() || originalSound.get().getUserId() != userId) {
            model.addAttribute("error", "No tienes permisos para editar este sonido");
            return "redirect:/sounds/" + soundId;
        }

        try {
            soundService.editSound(soundId, title, description, categories, audioFile, imageFile, username);
            return "redirect:/sounds/" + soundId;

        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/sounds/" + soundId + "/edit";
        }
    }

    @PostMapping("/sounds/{soundId}/delete")
    public String deleteSound(
            @PathVariable long soundId, // ID del sonido a eliminar
            HttpSession session, // Sesión del usuario
            RedirectAttributes redirectAttributes) { // Para enviar mensajes de retroalimentación

        // Obtener el ID del usuario actual desde la sesión
        Long userId = userService.getUserIdFromSession(session);

        // Verificar si el usuario está autenticado
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para eliminar un sonido.");
            return "redirect:/login"; // Redirigir al login si no está autenticado
        }

        // Buscar el sonido por su ID
        Optional<Sound> soundOptional = soundService.findSoundById(soundId);

        // Verificar si el sonido existe
        if (!soundOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El sonido no existe.");
            return "redirect:/sounds"; // Redirigir al dashboard si el sonido no existe
        }

        Sound sound = soundOptional.get();

        // Verificar permisos: el usuario debe ser el propietario
        boolean isOwner = userId != null && userId.equals(sound.getUserId());

        if (!isOwner) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar este sonido.");
            return "redirect:/sounds/" + soundId; // Redirigir a la página del sonido si no tiene permisos
        }

        // Eliminar el sonido
        commentService.deleteCommentsBySoundId(soundId);
        soundService.deleteSound(soundId);
        redirectAttributes.addFlashAttribute("success", "El sonido se ha eliminado correctamente.");

        return "redirect:/sounds"; // Redirigir al dashboard después de eliminar
    }

    @GetMapping("/sounds/audio/{id}")
    public ResponseEntity<byte[]> getAudio(@PathVariable Long id) {
        Optional<Sound> soundOptional = soundService.findSoundById(id);
        if (soundOptional.isPresent()) {
            Sound sound = soundOptional.get();
            try {
                Blob audioBlob = sound.getAudioBlob();
                if (audioBlob != null) {
                    InputStream inputStream = audioBlob.getBinaryStream();
                    byte[] audioBytes = inputStream.readAllBytes();
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType("audio/mpeg")) // Ajusta el tipo de medio si es diferente
                            .body(audioBytes);
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/sounds/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<Sound> soundOptional = soundService.findSoundById(id);
        if (soundOptional.isPresent()) {
            Sound sound = soundOptional.get();
            try {
                Blob imageBlob = sound.getImageBlob();
                if (imageBlob != null) {
                    InputStream inputStream = imageBlob.getBinaryStream();
                    byte[] imageBytes = inputStream.readAllBytes();
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG) // Ajusta el tipo de medio si es diferente (image/png, etc.)
                            .body(imageBytes);
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }
}