package es.swapsounds.service;

import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
        users.add(new User("admin", "admin@gmail.com", "admin123", "https://imgs.search.brave.com/VuBfiTd2u6sg7kwHVE-LzZGF_uwTzV8Hssy42MikWg8/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9yZXNv/dXJjZXMudGlkYWwu/Y29tL2ltYWdlcy8w/MTRlYWYzMi84NjY5/LzRmYTkvYWRiZi8z/ODRjZmUzMjRmZTYv/NjQweDY0MC5qcGc", idCounter++, null));
    }

    public Long getUserIdFromSession(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    public Optional<User> getUserById(Long userId) {
        return findUserById(userId);
    }

    public void updateUsername(Long userId, String newUsername) {
        updateUsername(userId, newUsername);
    }

    public void addUser(User user) {
        ((Category) users).setId(idCounter++);
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
        // Deletes the user from the list
        users.removeIf(u -> u.getUserId() == userId);

        // Deleting all the sounds of the user
        List<Sound> userSounds = sounds.stream()
                .filter(s -> s.getUserId() == userId)
                .collect(Collectors.toList());

        userSounds.forEach(sound -> {
            // Deletes the files stored locally
            try {
                Path audioPath = Paths.get("uploads" + sound.getFilePath().replace("/uploads/", "/"));
                Files.deleteIfExists(audioPath);

                Path imagePath = Paths.get("uploads" + sound.getImagePath().replace("/uploads/", "/"));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Error eliminando archivos: " + e.getMessage());
            }
        });
        sounds.removeAll(userSounds);

        // Profile Image deletion
        users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .ifPresent(u -> {
                    if (u.getProfilePicturePath() != null && !u.getProfilePicturePath().contains("default")) {
                        try {
                            Files.deleteIfExists(
                                    Paths.get("uploads" + u.getProfilePicturePath().replace("/uploads/", "/")));
                        } catch (IOException e) {
                            System.err.println("Error eliminando avatar: " + e.getMessage());
                        }
                    }
                });
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

}
