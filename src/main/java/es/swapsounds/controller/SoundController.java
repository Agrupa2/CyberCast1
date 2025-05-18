package es.swapsounds.controller;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import es.swapsounds.service.SoundService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import es.swapsounds.service.CategoryService;
import es.swapsounds.service.CommentService;

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
            Principal principal,
            Model model) {

        // Obtener usuario desde el UserService
        Optional<User> userOpt = userService.getUserFromPrincipal(principal);
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

        Page<Sound> firstPage = soundService.getFilteredSoundsPage(query, category, 0, 8);
        model.addAttribute("hasNext", firstPage.hasNext());
        model.addAttribute("currentPage", firstPage.getNumber());

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
    public String showUploadForm(Principal principal, Model model) {

        String username = (principal != null) ? principal.getName() : null;
        System.out.println("Accessing /sounds/upload with username: " + username);
        if (username == null) {
            model.addAttribute("error", "You must be logged in to upload sounds.");
            return "login";
        }

        // Get all existing categories
        List<Category> allCategories = categoryService.getAllCategories();

        // Add to model
        model.addAttribute("allCategories", allCategories);

        Optional<User> user = userService.findUserByUsername(username);
        if (user.isPresent()) {
            model.addAttribute("userId", user.get().getUserId());
            model.addAttribute("username", username);
            System.out.println("User found: " + username + ", userId: " + user.get().getUserId());
            return "upload-sound";
        } else {
            System.out.println("User not found for username: " + username);
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
            Principal principal,
            Model model) throws IOException {

        if (principal == null) {
            model.addAttribute("error", "You must be logged in to upload sounds.");
            return "redirect:/login";
        }
        Long userId = userService.findUserByUsername(principal.getName()).get().getUserId();
        Optional<User> user = userService.findUserById(userId);
        User uploader = user.get();
        if (!user.isPresent()) {
            model.addAttribute("error", "User not found.");
            principal = null;
            return "redirect:/login";
        }

        boolean validFiles = soundService.validateFiles(audioFile, imageFile);

        if (validFiles) {
            soundService.createSound(title, description, categories, audioFile, imageFile, uploader);
        }

        model.addAttribute("success", "Sound uploaded successfully!");
        return "redirect:/sounds/" + soundService.getLastInsertedSoundId();
    }

    @GetMapping("/sounds/download")
    public String downloadSounds(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", defaultValue = "all") String category,
            Principal principal,
            Model model) {

        // Get user from UserService
        Optional<User> userOpt = userService.getUserFromPrincipal(principal);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userId", user.getUserId());
        }

        // Get filtered sounds using SoundService
        List<Sound> filteredSounds = soundService.getFilteredSounds(query, category);
        model.addAttribute("sounds", filteredSounds);
        model.addAttribute("query", query);
        model.addAttribute("category", category);

        // Get categories for the dropdown
        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("allCategories", allCategories);

        model.addAttribute("selectedAll", "all".equalsIgnoreCase(category));
        for (Category cat : allCategories) {
            model.addAttribute("selected" + cat.getName(), category.equalsIgnoreCase(cat.getName()));
        }

        return "download-sound";
    }

    @GetMapping("/sounds/{soundId}")
    public String soundDetails(@PathVariable long soundId, Principal principal, Model model, HttpServletRequest request) {
        // Manejar usuario autenticado o no
        String username = principal != null ? principal.getName() : null;
        Long userId = null;
        boolean isAdmin = false;

        if (principal != null) {
            Optional<User> userOpt = userService.findUserByUsername(principal.getName());
            if (userOpt.isPresent()) {
                userId = userOpt.get().getUserId();
                // Check if the user has the ADMIN role
                isAdmin = request.isUserInRole("ADMIN");
            } else {
                throw new IllegalArgumentException("Usuario no encontrado");
            }
        }

        // Buscar el sonido
        Sound sound = soundService.findSoundById(soundId)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado"));

        // Añadir sonido al modelo
        model.addAttribute("sound", sound);

        // Get uploader information
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

        // Obtener comentarios con información de permisos para edición/eliminación
        List<Map<String, Object>> commentsWithImages = commentService.getCommentsWithImagesBySoundId(soundId, userId);
        // Add isOwner or isAdmin flag to each comment
        for (Map<String, Object> comment : commentsWithImages) {
            Long commentUserId = (Long) comment.get("userId");
            boolean canEditComment = (userId != null && userId.equals(commentUserId)) || isAdmin;
            comment.put("canEditComment", canEditComment);
        }
        model.addAttribute("comments", commentsWithImages);

        // Get categories
        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("allCategories", allCategories);
        Set<String> selectedCategories = soundService.getSelectedCategoryNames(sound);
        model.addAttribute("selectedCategories", selectedCategories);

        // Determinar si el usuario puede editar/eliminar el sonido (propietario o
        // admin)
        boolean canEditSound = (userId != null && userId.equals(sound.getUserId())) || isAdmin;
        model.addAttribute("canEditSound", canEditSound);
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
            Principal principal,
            Model model) throws IOException {
        if (principal == null) {
            throw new IllegalArgumentException("You must be logged in to edit a sound");
        }

        String username = principal.getName();
        Optional<User> userOpt = userService.findUserByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        Long userId = userOpt.get().getUserId();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        Optional<Sound> originalSound = soundService.findSoundById(soundId);
        if (originalSound.isEmpty()) {
            throw new IllegalArgumentException("Resource not found");
        }

        if (!userId.equals(originalSound.get().getUserId()) && !isAdmin) {
            throw new IllegalArgumentException("You do not have permission to edit this sound");
        }

        // Crear una política que solo permita texto plano (sin etiquetas HTML)
        boolean validFiles = soundService.validateFiles(audioFile, imageFile);

        try {
            if (validFiles) {
                soundService.editSound(soundId, title, description, categories, audioFile, imageFile, username);
                return "redirect:/sounds/" + soundId;
            }else{
                throw new IllegalArgumentException("Archivos inválidos");
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid data to update the sound");
        } catch (IOException e) {
            throw new IllegalArgumentException("Error processing files");
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating the sound");
        }
    }

    @PostMapping("/sounds/{soundId}/delete")
    public String deleteSound(
            @PathVariable long soundId,
            Principal principal,
            Model model) {
        if (principal == null) {
            throw new IllegalArgumentException("You must be logged in to delete a sound");
        }

        String username = principal.getName();
        Optional<User> userOpt = userService.findUserByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        Long userId = userOpt.get().getUserId();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        Optional<Sound> soundOptional = soundService.findSoundById(soundId);
        if (soundOptional.isEmpty()) {
            throw new IllegalArgumentException("Resource not found");
        }
        Sound sound = soundOptional.get();

        if (!userId.equals(sound.getUserId()) && !isAdmin) {
            throw new IllegalArgumentException("You do not have permission to delete this sound");
        }

        try {
            commentService.deleteCommentsBySoundId(soundId);
            soundService.deleteSound(soundId);
            return "redirect:/sounds";
        } catch (Exception e) {
            throw new IllegalArgumentException("Error deleting the sound");
        }
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
                            .contentType(MediaType.parseMediaType("audio/mpeg")) // Adjust media type if different
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
                            .contentType(MediaType.IMAGE_JPEG) // Adjust media type if different (image/png, etc.)
                            .body(imageBytes);
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }

    //Adding a knew / to make dynamic queries work


    @GetMapping("/sounds/search")
    public String searchSounds(Model model,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String duration,
        @RequestParam(required = false) Long userId) {

        List<Sound> sounds;

        if (title == null && category == null && duration == null && userId == null) {
            sounds = soundService.getAllSounds();
        } else {
            sounds = soundService.searchSounds(title, category, duration, userId);
        }

        model.addAttribute("sounds", sounds);
        model.addAttribute("selectedAll", false);
        return "sounds";
    }




    
    @GetMapping("/sounds/upload/secret")
    public String showUploadSecretSoundForm(Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("error", "You must be logged in to upload secret sounds.");
            return "login";
        }
        String username = principal.getName();
        Optional<User> userOpt = userService.findUserByUsername(username);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "User not found. Please login again.");
            return "login";
        }

        model.addAttribute("username", username);
        model.addAttribute("userId", userOpt.get().getUserId());
        model.addAttribute("allCategories", categoryService.getAllCategories());
        model.addAttribute("isSecret", true);

        return "upload-secret-sound";
    }
}