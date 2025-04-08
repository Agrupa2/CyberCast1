package es.swapsounds.service;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.SoundRepository;
import es.swapsounds.storage.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final SoundRepository soundRepository;

    public UserService(UserRepository userRepository, SoundRepository soundRepository) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;

//        public UserService() {
//            // Locally generated users for testing
//            users.add(new User("user", "user@gmail.com", "user123", null, idCounter++, null));
//            users.add(new User("admin", "admin@gmail.com", "admin123",
//                    "https://imgs.search.brave.com/VuBfiTd2u6sg7kwHVE-LzZGF_uwTzV8Hssy42MikWg8/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9yZXNv/dXJjZXMudGlkYWwu/Y29tL2ltYWdlcy8w/MTRlYWYzMi84NjY5/LzRmYTkvYWRiZi8z/ODRjZmUzMjRmZTYv/NjQweDY0MC5qcGc",
//                    idCounter++, null));
//        }

    }

    public Long getUserIdFromSession(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    public Optional<User> getUserFromSession(HttpSession session) {
        Long userId = getUserIdFromSession(session);
        return (userId != null) ? userRepository.findById(userId) : Optional.empty();
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public void updateUsername(long userId, String newUsername) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setUsername(newUsername);
            userRepository.save(user);
        });
    }

    public void updateProfilePicture(long userId, String imagePath) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setProfilePicturePath(imagePath);
            userRepository.save(user);
        });
    }

    public void addUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> authenticate(String emailOrUsername, String password) {
        return userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername)
                .filter(user -> user.getPassword().equals(password));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(long userId) {
        return userRepository.findById(userId);
    }

    public void deleteUser(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 1. Borrar sonidos del usuario y archivos
            List<Sound> userSounds = soundRepository.findByUserId(userId);
            for (Sound sound : userSounds) {
                try {
                    Path audioPath = Paths.get("uploads", sound.getFilePath().replace("/uploads/", ""));
                    Files.deleteIfExists(audioPath);

                    Path imagePath = Paths.get("uploads", sound.getImagePath().replace("/uploads/", ""));
                    Files.deleteIfExists(imagePath);
                } catch (IOException e) {
                    System.err.println("Error eliminando archivos del sonido: " + e.getMessage());
                }
            }

            soundRepository.deleteAll(userSounds);

            // 2. Borrar imagen de perfil si no es default
            String profilePath = user.getProfilePicturePath();
            if (profilePath != null && !profilePath.contains("default")) {
                try {
                    Path avatarPath = Paths.get("uploads", profilePath.replace("/uploads/", ""));
                    Files.deleteIfExists(avatarPath);
                } catch (IOException e) {
                    System.err.println("Error eliminando imagen de perfil: " + e.getMessage());
                }
            }

            // 3. Borrar usuario
            userRepository.deleteById(userId);
        }
    }

    public Map<String, String> getProfileInfo(User user) {
        Map<String, String> info = new HashMap<>();
        String profileImage = user.getProfilePicturePath();
        String userInitial = "";

        if (profileImage == null || profileImage.isEmpty()) {
            userInitial = (user.getUsername() != null && !user.getUsername().isEmpty())
                    ? user.getUsername().substring(0, 1).toUpperCase()
                    : "?";
        }

        info.put("profileImagePath", profileImage);
        info.put("userInitial", userInitial);
        return info;
    }
}
