package es.swapsounds.service;

import es.swapsounds.model.User;
import es.swapsounds.storage.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Registers a new user.
     * Verifies if the username or email is already in use and if the password is valid.
     */
    public User registerUser(String username, String email, String password, MultipartFile profilePhoto) throws IOException {
    // Verificar si el nombre de usuario o correo ya está registrado
    List<User> existingUsers = userRepository.findByUsernameOrEmail(username, email);
    if (!existingUsers.isEmpty()) {
        throw new IllegalArgumentException("El nombre de usuario o correo ya está registrado");
    }

    // Verificar que la contraseña tenga al menos 8 caracteres
    if (password.length() < 8) {
        throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
    }

    // Crear el usuario
    User user = new User(username, email, password);

    // Si se proporciona una foto de perfil, convertirla a Blob
    if (profilePhoto != null && !profilePhoto.isEmpty()) {
        try {
            Blob photoBlob = new SerialBlob(profilePhoto.getBytes());
            user.setProfilePicture(photoBlob);
        } catch (SQLException e) {
            throw new IOException("Error al convertir la imagen a Blob: " + e.getMessage());
        }
    } else {
        // Opcional: asignar una imagen por defecto como Blob si no se proporciona ninguna
        // Por ahora, dejaremos profilePicture como null
    }

    // Guardar y devolver el usuario
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
