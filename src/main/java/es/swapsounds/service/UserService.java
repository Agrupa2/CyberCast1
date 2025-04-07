package es.swapsounds.service;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private List<User> users = new ArrayList<>();
    private List<Sound> sounds = new ArrayList<>();

    private long idCounter = 1;

    public UserService() {
        // Locally generated users for testing
        users.add(new User("user", "user@gmail.com", "user123", null, idCounter++, null));
        users.add(new User("admin", "admin@gmail.com", "admin123",
                "https://imgs.search.brave.com/VuBfiTd2u6sg7kwHVE-LzZGF_uwTzV8Hssy42MikWg8/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9yZXNv/dXJjZXMudGlkYWwu/Y29tL2ltYWdlcy8w/MTRlYWYzMi84NjY5/LzRmYTkvYWRiZi8z/ODRjZmUzMjRmZTYv/NjQweDY0MC5qcGc",
                idCounter++, null));
    }

    public Long getUserIdFromSession(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    public Optional<User> getUserFromSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            return findUserById(userId);
        }
        return Optional.empty();
    }

    public Optional<User> getUserById(Long userId) {
        return findUserById(userId);
    }

    public void updateUsername(Long userId, String newUsername) {
        updateUsername(userId, newUsername);
    }

    public void addUser(User user) {
        user.setId(idCounter++);
        users.add(user);
    }

    public Optional<User> findUserByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    public Optional<User> findUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public Optional<User> authenticate(String email, String password) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email)
                        || u.getUsername().equals(email) && u.getPassword().equals(password))
                .findFirst();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public Optional<User> findUserById(long userId) {
        return users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst();
    }

    public void deleteUser(long userId) {
        // 1. Buscar el usuario antes de borrarlo
        Optional<User> userOptional = users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst();

        // 2. Borrar sus sonidos y archivos relacionados
        List<Sound> userSounds = sounds.stream()
                .filter(s -> s.getUserId() == userId)
                .collect(Collectors.toList());

        userSounds.forEach(sound -> {
            try {
                // Eliminar archivo de audio
                Path audioPath = Paths.get("uploads", sound.getFilePath().replace("/uploads/", ""));
                Files.deleteIfExists(audioPath);

                // Eliminar imagen del sonido
                Path imagePath = Paths.get("uploads", sound.getImagePath().replace("/uploads/", ""));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Error eliminando archivos del sonido: " + e.getMessage());
            }
        });

        // Quitar sonidos del sistema
        sounds.removeAll(userSounds);

        // 3. Eliminar imagen de perfil si no es la default
        userOptional.ifPresent(user -> {
            if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().contains("default")) {
                try {
                    Path avatarPath = Paths.get("uploads", user.getProfilePicturePath().replace("/uploads/", ""));
                    Files.deleteIfExists(avatarPath);
                } catch (IOException e) {
                    System.err.println("Error eliminando imagen de perfil: " + e.getMessage());
                }
            }
        });

        // 4. Finalmente, eliminar el usuario
        users.removeIf(u -> u.getUserId() == userId);
    }

    public void updateUsername(long userId, String newUsername) {
        users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .ifPresent(u -> u.setUsername(newUsername));
    }

    public void updateProfilePicture(long userId, String imagePath) {
        users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .ifPresent(u -> u.setProfilePicturePath(imagePath));
    }

    public Map<String, String> getProfileInfo(User user) {
        Map<String, String> info = new HashMap<>();
        String profileImage = user.getProfilePicturePath();
        String userInitial = "";
        if (profileImage == null || profileImage.isEmpty()) {
            // Si no hay imagen, se usa la inicial del nombre
            userInitial = (user.getUsername() != null && !user.getUsername().isEmpty())
                    ? user.getUsername().substring(0, 1).toUpperCase()
                    : "?";
        }
        info.put("profileImagePath", profileImage);
        info.put("userInitial", userInitial);
        return info;
    }

}
