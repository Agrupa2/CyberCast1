package es.swapsounds.service;

import es.swapsounds.model.User;
import es.swapsounds.storage.UserRepository;
import es.swapsounds.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AuthService {

    @Autowired
    private InMemoryStorage storage;

    @Autowired
    private UserRepository userRepository;

    /**
     * Registers a new user.
     * Verifies if the username or email is already in use and if the password is valid.
     */
    public User registerUser(String username, String email, String password, MultipartFile profilePhoto) throws IOException {
        // Verify if the username or email is already registered
        List<User> existingUsers = userRepository.findByUsernameOrEmail(username, email);
        if (!existingUsers.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario o correo ya está registrado");
        }

        // Verify that the password has at least 8 characters
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        // // Upload the profile photo if provided
        String photoPath = (profilePhoto != null && !profilePhoto.isEmpty())
                ? storage.saveFile(username, profilePhoto, "profiles")
                : "/uploads/profiles/default-avatar.png";

        // Create and store the user
        User user = new User(username, email, password, photoPath);
        return userRepository.save(user);
    }

    /**
     * * Authenticates a user using their username or email and password.
     */
    public User authenticate(String emailOrUsername, String password) {
        List<User> users = userRepository.findByUsernameOrEmail(emailOrUsername, emailOrUsername);
        if (users.isEmpty()) {
            return null;
        }

        User user = users.get(0);
        if (user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }
}
