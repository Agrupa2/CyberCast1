package es.swapsounds.service;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.SoundRepository;
import es.swapsounds.storage.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SoundRepository soundRepository;

    public UserService(UserRepository userRepository, SoundRepository soundRepository) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
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

    public void updateProfilePicture(long userId, MultipartFile profilePhoto) throws IOException {
        userRepository.findById(userId).ifPresent(user -> {
            try {
                if (profilePhoto != null && !profilePhoto.isEmpty()) {
                    // Convertir el MultipartFile a Blob
                    Blob photoBlob = new SerialBlob(profilePhoto.getBytes());
                    user.setProfilePicture(photoBlob); // Asume que el campo en User es 'profilePicture' de tipo Blob
                } else {
                    // Si no se proporciona imagen, asignamos null (o podrías mantener la imagen actual)
                    user.setProfilePicture(null);
                }
                userRepository.save(user);
            } catch (SQLException e) {
                throw new RuntimeException("Error al convertir la imagen a Blob: " + e.getMessage());
            } catch (IOException e) {
                // Manejar el error, por ejemplo, lanzando una excepción personalizada
                throw new RuntimeException("Error al leer los bytes del archivo: " + e.getMessage());
            }
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
            // 1. Borrar sonidos del usuario
            List<Sound> userSounds = soundRepository.findByUserId(userId);
            soundRepository.deleteAll(userSounds);

            // 2. Borrar el usuario
            userRepository.deleteById(userId);
        }
    }

    public Map<String, String> getProfileInfo(User user) {
        Map<String, String> info = new HashMap<>();
        String profileImageBase64 = "";
        String userInitial = "";

        Blob profilePicture = user.getProfilePicture(); // Asume que el campo en User es 'profilePicture' de tipo Blob
        if (profilePicture != null) {
            try {
                // Convertir el Blob a bytes
                byte[] imageBytes = profilePicture.getBytes(1, (int) profilePicture.length());
                // Convertir los bytes a Base64 con prefijo para uso en HTML
                profileImageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
            } catch (SQLException e) {
                System.err.println("Error al convertir el Blob a Base64: " + e.getMessage());
            }
        }

        if (profileImageBase64.isEmpty()) {
            userInitial = (user.getUsername() != null && !user.getUsername().isEmpty())
                    ? user.getUsername().substring(0, 1).toUpperCase()
                    : "?";
        }

        info.put("profileImageBase64", profileImageBase64);
        info.put("userInitial", userInitial);
        return info;
    }
}