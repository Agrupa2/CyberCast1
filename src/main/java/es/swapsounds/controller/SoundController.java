package es.swapsounds.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import es.swapsounds.service.SoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.swapsounds.dto.CommentView;
import es.swapsounds.model.Category;
import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.service.UserService;
import es.swapsounds.service.CategoryService;
import es.swapsounds.service.CommentService;
import es.swapsounds.storage.InMemoryStorage;
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
    private InMemoryStorage storage;

    @Autowired
    private UserService userService;

    @GetMapping("/start")
    public String showSounds(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", defaultValue = "all") String category,
            HttpSession session,
            Model model) {

        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("userId");

        if (username != null && userId != null) {
            model.addAttribute("message", "Welcome, " + username + "!");
            model.addAttribute("username", username);
            model.addAttribute("userId", userId);
        }

        // Obtener todos los sonidos almacenados localmente
        List<Sound> allSounds = soundService.getAllSounds();

        allSounds.forEach(sound -> System.out.println("Sound: " + sound.getTitle() + ", Categories: " +
                (sound.getCategories() != null
                        ? sound.getCategories().stream().map(Category::getName).collect(Collectors.toList())
                        : "None")));

        // Aplicar filtros para la barra de búsqueda
        List<Sound> filteredSounds = allSounds.stream()
                .filter(sound -> {
                    // 1. Filtro de categoría
                    boolean matchesCategory = true;
                    if (!category.equals("all")) {
                        matchesCategory = sound.getCategories() != null && sound.getCategories().stream()
                                .anyMatch(cat -> cat.getName().equalsIgnoreCase(category));
                    }

                    // 2. Filtro de búsqueda
                    boolean matchesQuery = query == null || query.trim().isEmpty() ||
                            sound.getTitle().toLowerCase().contains(query.toLowerCase());

                    return matchesCategory && matchesQuery;
                })
                .collect(Collectors.toList());

        // Obtener todas las categorías para el dropdown
        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("sounds", filteredSounds);
        model.addAttribute("query", query);
        model.addAttribute("category", category);
        model.addAttribute("allCategories", allCategories);

        // Ajustar la lógica de selección para el dropdown
        model.addAttribute("selectedAll", category.equals("all"));
        for (Category cat : allCategories) {
            model.addAttribute("selected" + cat.getName(), category.equalsIgnoreCase(cat.getName()));
        }

        return "start";
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
        if (!user.isPresent()) {
            model.addAttribute("error", "Usuario no encontrado.");
            session.invalidate();
            return "redirect:/login";
        }

        // Calcular duración
        String duration = soundService.calculateDuration(audioFile);

        // Guardar archivos
        String username = user.get().getUsername();
        String audioPath = storage.saveFile(username, audioFile, "sounds");
        String imagePath = storage.saveFile(username, imageFile, "images");

        // Crear el sonido con duración calculada
        Sound sound = new Sound(
                0,
                title,
                description,
                audioPath,
                imagePath,
                user.get(),
                new ArrayList<>(),
                duration);

        // Procesar categorías
        for (String categoryName : categories) {
            Category category = categoryService.findOrCreateCategory(categoryName);
            sound.getCategories().add(category);
            category.getSounds().add(sound);
        }

        soundService.addSound(sound);

        model.addAttribute("success", "¡Sonido subido con éxito!");
        return "redirect:/sounds/" + sound.getSoundId();
    }


    @GetMapping("/sounds/download")
    public String showDownloadSounds(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", defaultValue = "all") String category,
            HttpSession session,
            Model model) {

        // Obtainint username and userId from session
        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("userId");

        // If the user is logged, it adds ths info to the model
        if (username != null && userId != null) {
            model.addAttribute("message", "Welcome, " + username + "!");
            model.addAttribute("username", username);
            model.addAttribute("userId", userId);
        }

        // Obtaining every sound
        List<Sound> allSounds = soundService.getAllSounds();

        // Filtering sounds by the user search or category selected
        List<Sound> filteredSounds = allSounds.stream()
                .filter(sound -> {
                    // 1. Filtro de categoría modificado
                    boolean matchesCategory = category.equals("all") ||
                            sound.getCategories().stream()
                                    .anyMatch(cat -> cat.getName().equalsIgnoreCase(category));

                    // 2. Filtro de búsqueda (se mantiene igual)
                    boolean matchesQuery = query == null ||
                            sound.getTitle().toLowerCase().contains(query.toLowerCase());

                    return matchesCategory && matchesQuery;
                })
                .collect(Collectors.toList());

        // Adding the filtered sounds to the model
        model.addAttribute("sounds", filteredSounds);

        // Keeping the search and category selected by the user
        model.addAttribute("query", query);
        model.addAttribute("category", category);

        // Keeping the selected category
        model.addAttribute("selectedAll", category.equals("all"));
        model.addAttribute("selectedMeme", category.equalsIgnoreCase("Meme"));
        model.addAttribute("selectedFootball", category.equalsIgnoreCase("Football"));
        model.addAttribute("selectedParty", category.equalsIgnoreCase("Party"));

        return "download-sound";
    }

    @GetMapping("/sounds/{soundId}")
    public String soundDetails(
            @PathVariable long soundId,
            HttpSession session,
            Model model) {

        // Obtaining userId and username from the session
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");

        Optional<Sound> soundOpt = soundService.findSoundById(soundId);
        if (!soundOpt.isPresent()) {
            return "redirect:/start";
        }

        Sound sound = soundOpt.get();
        Optional<User> uploader = userService.findUserById(sound.getUserId());

        String userInitial = "?"; // default value
        String profileImagePath = null; // default profileImagePath set to null

        if (uploader.isPresent()) {
            User user = uploader.get();

            // Assigning to profileImagePath the profile picture of the user
            profileImagePath = user.getProfilePicturePath();

            if (profileImagePath == null) {
                userInitial = user.getUsername().length() > 0
                        ? user.getUsername().substring(0, 1).toUpperCase()
                        : "?";
            }

            model.addAttribute("uploader", user);
        } else {
            model.addAttribute("uploader", null);
        }

        List<Comment> comments = commentService.getCommentsBySoundId(soundId);

        // Checking if the user is the owner of the sound
        Long currentUserId = (Long) session.getAttribute("userId");
        List<CommentView> commentViews = comments.stream()
                .map(comment -> {
                    boolean isOwner = currentUserId != null &&
                            comment.getUser().getUserId() == currentUserId;
                    return new CommentView(comment, isOwner);
                })
                .collect(Collectors.toList());

        List<Category> allCategories = categoryService.getAllCategories();

        model.addAttribute("allCategories", allCategories);
        model.addAttribute("selectedCategories", sound.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet()));

        model.addAttribute("comments", commentViews);
        model.addAttribute("userInitial", userInitial);
        model.addAttribute("profileImagePath", profileImagePath); // Añadir profileImagePath al modelo
        model.addAttribute("sound", sound);
        model.addAttribute("username", username); // Pasar username al template
        model.addAttribute("isOwner", userId != null && userId == soundOpt.get().getUserId());

        return "sound-details";
    }

    @PostMapping("/sounds/{soundId}/edit")
    public String editSound(
            @PathVariable long soundId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Set<String> categories, // Cambiado a Set<String>
            @RequestParam(required = false) MultipartFile audioFile,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpSession session,
            Model model) throws IOException {

        Long userId = (Long) session.getAttribute("userId");
        Optional<Sound> originalSound = soundService.findSoundById(soundId);

        // Validación de permisos
        if (userId == null || !originalSound.isPresent() || originalSound.get().getUserId() != userId) {
            model.addAttribute("error", "No tienes permisos para editar este sonido");
            return "redirect:/sounds/" + soundId;
        }

        Sound sound = originalSound.get();
        String username = (String) session.getAttribute("username");

        try {
            // 1. Limpiar categorías existentes (bidireccional)
            sound.getCategories().forEach(cat -> cat.getSounds().remove(sound));
            sound.getCategories().clear();

            // 2. Actualizar campos básicos
            sound.setTitle(title);
            sound.setDescription(description);

            // 3. Procesar nuevas categorías
            categories.forEach(catName -> {
                Category category = categoryService.findOrCreateCategory(catName);
                category.getSounds().add(sound);
                sound.addCategory(category);
            });

            // 4. Manejar archivos
            if (audioFile != null && !audioFile.isEmpty()) {
                String newAudioPath = storage.saveFile(username, audioFile, "sounds");
                sound.setFilePath(newAudioPath);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String newImagePath = storage.saveFile(username, imageFile, "images");
                sound.setImagePath(newImagePath);
            }

            soundService.updateSound(sound);
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
            return "redirect:/dashboard"; // Redirigir al dashboard si el sonido no existe
        }

        Sound sound = soundOptional.get();

        // Verificar permisos: el usuario debe ser el propietario o un administrador
        boolean isOwner = sound.getUserId() == userId;

        if (!isOwner) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar este sonido.");
            return "redirect:/sounds/" + soundId; // Redirigir a la página del sonido si no tiene permisos
        }

        // Eliminar el sonido
        commentService.deleteCommentsBySoundId(soundId);
        soundService.deleteSound(soundId);
        redirectAttributes.addFlashAttribute("success", "El sonido se ha eliminado correctamente.");

        return "redirect:/start"; // Redirigir al dashboard después de eliminar
    }
}

        /* @PostMapping("/sounds/{id}/delete")
        public String deleteSound(
                @PathVariable Long id,
                HttpSession session,
                Model model) {

            Long userId = userService.getUserIdFromSession(session);
            Optional<Sound> sound = soundService.findSoundById(id);

            if (userId == null || !sound.isPresent() || sound.get().getUserId() != userId) {
                model.addAttribute("error", "No tienes permisos para esta acción");
                return "redirect:/start";
            }

            soundService.deleteSound(id);
            return "redirect:/start";
        } */

