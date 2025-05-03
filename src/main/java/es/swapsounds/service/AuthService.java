package es.swapsounds.service;

import es.swapsounds.model.User;
import es.swapsounds.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Registers a new user.
     * Verifies if the username or email is already in use and if the password is
     * valid.
     */
    public User registerUser(String username, String email, String password, MultipartFile profilePhoto)
            throws IOException {
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
        User user = new User(username, email, password, null);

        // Si se proporciona una foto de perfil, convertirla a Blob
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                Blob photoBlob = new SerialBlob(profilePhoto.getBytes());
                user.setProfilePicture(photoBlob);
            } catch (SQLException e) {
                throw new IOException("Error al convertir la imagen a Blob: " + e.getMessage());
            }
        } else {
            // Opcional: asignar una imagen por defecto como Blob si no se proporciona
            // ninguna
            // Por ahora, dejaremos profilePicture como null
        }

        // Guardar y devolver el usuario
        return userRepository.save(user);
    }

    /**
     * * Authenticates a user using their username or email and password.
     */
    public Optional<User> authenticate(String emailOrUsername, String password) {
        Optional<User> userOptional = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("Usuario encontrado: " + user.getUsername() + ", Contraseña en DB: " + user.getPassword()
                    + ", Contraseña introducida: " + password);
            if (user.getPassword().equals(password)) {
                System.out.println("¡Contraseña coincide!");
                return userOptional;
            } else {
                System.out.println("Contraseña incorrecta para el usuario: " + user.getUsername());
                return Optional.empty();
            }
        } else {
            System.out.println("No se encontró ningún usuario con el email o username: " + emailOrUsername);
            return Optional.empty();
        }
    }
}
