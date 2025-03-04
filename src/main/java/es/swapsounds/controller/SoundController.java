package es.swapsounds.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.CommentRepository;
import es.swapsounds.storage.InMemoryStorage;
import jakarta.servlet.http.HttpSession;

@Controller
public class SoundController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private InMemoryStorage storage;

    @GetMapping("/start")
    public String showSounds(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", defaultValue = "all") String category,
            HttpSession session,
            Model model) {

        String username = (String) session.getAttribute("username");
        Integer userId = (Integer) session.getAttribute("userId");

        if (username != null && userId != null) {
            model.addAttribute("message", "Welcome, " + username + "!");
            model.addAttribute("username", username);
            model.addAttribute("userId", userId);
        }

        // Obtaining everyt single sound locally stored
        List<Sound> allSounds = storage.getAllSounds();

        // Applying filters for the search bar
        List<Sound> filteredSounds = allSounds.stream()
                .filter(sound -> {
                    boolean matchesCategory = category.equals("all")
                            || sound.getCategory().equalsIgnoreCase(category);

                    boolean matchesQuery = query == null
                            || sound.getTitle().toLowerCase().contains(query.toLowerCase());

                    return matchesCategory && matchesQuery;
                })
                .collect(Collectors.toList());

        // Preparomg the model with the user selection
        model.addAttribute("sounds", filteredSounds);
        model.addAttribute("query", query);
        model.addAttribute("category", category);

        // Applying the selected category
        model.addAttribute("selectedAll", category.equals("all"));
        model.addAttribute("selectedMeme", category.equalsIgnoreCase("Meme"));
        model.addAttribute("selectedFootball", category.equalsIgnoreCase("Football"));
        model.addAttribute("selectedParty", category.equalsIgnoreCase("Party"));

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
            HttpSession session, // Using HttpSession in order to get UserId
            Model model) throws IOException {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            model.addAttribute("error", "You must be logged in to upload sounds.");
            return "redirect:/login"; // If the user is not logged in, redirect to the login page
        }

        Optional<User> user = storage.findUserById(userId);
        if (!user.isPresent()) {
            model.addAttribute("error", "User not found. Please login again.");
            session.invalidate(); // Deleting invalid session
            return "redirect:/login";
        }

        // Storing files using username
        String username = user.get().getUsername();
        String audioPath = storage.saveFile(username, audioFile, "sounds");
        String imagePath = storage.saveFile(username, imageFile, "images");

        // Creating and stroring the sound
        Sound sound = new Sound(0, title, description, audioPath, imagePath, userId, category, duration);
        storage.addSound(sound);

        model.addAttribute("success", "Sound uploaded successfully!");
        model.addAttribute("username", username);
        model.addAttribute("userId", userId);
        return "upload-sound";
    }

    @GetMapping("/sounds/download")
    public String showDownloadSounds(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", defaultValue = "all") String category,
            HttpSession session,
            Model model) {

        // Obtainint username and userId from session
        String username = (String) session.getAttribute("username");
        Integer userId = (Integer) session.getAttribute("userId");

        // If the user is logged, it adds ths info to the model
        if (username != null && userId != null) {
            model.addAttribute("message", "Welcome, " + username + "!");
            model.addAttribute("username", username);
            model.addAttribute("userId", userId);
        }

        // Obtaining every sound
        List<Sound> allSounds = storage.getAllSounds();

        // Filtering sounds by the user search or category selected
        List<Sound> filteredSounds = allSounds.stream()
                .filter(sound -> {
                    boolean matchesCategory = category.equals("all")
                            || sound.getCategory().equalsIgnoreCase(category);

                    boolean matchesQuery = query == null
                            || sound.getTitle().toLowerCase().contains(query.toLowerCase());

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
            @PathVariable int soundId,
            HttpSession session,
            Model model) {

        // Obtaining userId and username from the session
        Integer userId = (Integer) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");

        Optional<Sound> soundOpt = storage.findSoundById(soundId);
        if (!soundOpt.isPresent()) {
            return "redirect:/start";
        }

        Sound sound = soundOpt.get();
        Optional<User> uploader = storage.findUserById(sound.getUserId());

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

        List<Comment> comments = commentRepository.getCommentsBySoundId(soundId);

        // Checking if the user is the owner of the sound
        Integer currentUserId = (Integer) session.getAttribute("userId");
        List<CommentView> commentViews = comments.stream()
                .map(comment -> {
                    boolean isOwner = currentUserId != null &&
                            comment.getUser().getUserId() == currentUserId;
                    return new CommentView(comment, isOwner);
                })
                .collect(Collectors.toList());

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
            @PathVariable int soundId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam(required = false) MultipartFile audioFile,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpSession session,
            Model model) throws IOException {

        Integer userId = (Integer) session.getAttribute("userId");
        Optional<Sound> originalSound = storage.findSoundById(soundId);

        if (userId == null || !originalSound.isPresent() || originalSound.get().getUserId() != userId) {
            model.addAttribute("error", "No tienes permisos para editar este sonido");
            return "redirect:/sounds/" + soundId;
        }

        Sound sound = originalSound.get();
        String username = (String) session.getAttribute("username");

        // Updating the sound with the new values
        sound.setTitle(title);
        sound.setDescription(description);
        sound.setCategory(category);

        // Dealing with new audio file uploaded in the edition form
        if (!audioFile.isEmpty()) {
            String newAudioPath = storage.saveFile(username, audioFile, "sounds");
            sound.setFilePath(newAudioPath);
        }

        // Dealing with new image file uploaded in the edition form
        if (!imageFile.isEmpty()) {
            String newImagePath = storage.saveFile(username, imageFile, "images");
            sound.setImagePath(newImagePath);
        }

        storage.updateSound(sound);
        return "redirect:/sounds/" + soundId;
    }

        @PostMapping("/sounds/{id}/delete")
    public String deleteSound(
            @PathVariable int id, // ID del sonido a eliminar
            HttpSession session, // Sesión del usuario
            RedirectAttributes redirectAttributes) { // Para enviar mensajes de retroalimentación

        // Obtener el ID del usuario actual desde la sesión
        Integer userId = (Integer) session.getAttribute("userId");

        // Verificar si el usuario está autenticado
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para eliminar un sonido.");
            return "redirect:/login"; // Redirigir al login si no está autenticado
        }

        // Buscar el sonido por su ID
        Optional<Sound> soundOptional = storage.findSoundById(id);

        // Verificar si el sonido existe
        if (!soundOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El sonido no existe.");
            return "redirect:/dashboard"; // Redirigir al dashboard si el sonido no existe
        }

        Sound sound = soundOptional.get();

        // Verificar permisos: el usuario debe ser el propietario o un administrador
        boolean isOwner = sound.getUserId() == userId;
        boolean isAdmin = "admin".equals(session.getAttribute("role"));

        if (!isOwner && !isAdmin) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar este sonido.");
            return "redirect:/sounds/" + id; // Redirigir a la página del sonido si no tiene permisos
        }

        // Eliminar el sonido
        storage.deleteSound(id);
        redirectAttributes.addFlashAttribute("success", "El sonido se ha eliminado correctamente.");

        return "redirect:/dashboard"; // Redirigir al dashboard después de eliminar
    }
}


