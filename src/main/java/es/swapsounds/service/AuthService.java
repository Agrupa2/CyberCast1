package es.swapsounds.service;

import es.swapsounds.model.User;
import es.swapsounds.repository.UserRepository;
import es.swapsounds.security.jwt.AuthResponse;
import es.swapsounds.security.jwt.AuthResponse.Status;
import es.swapsounds.security.jwt.LoginRequest;
import es.swapsounds.security.jwt.UserLoginService;
import jakarta.servlet.http.HttpServletResponse;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserLoginService userService;

    /**
     * Registers a new user.
     * Checks if the username or email is already in use and if the password is
     * valid.
     */
    public User registerUser(String username, String email, String password, MultipartFile profilePhoto)
            throws IOException {
        // Check if the username or email is already in use
        List<User> existingUsers = userRepository.findByUsernameOrEmail(username, email);
        if (!existingUsers.isEmpty()) {
            throw new IllegalArgumentException("Username or email is already registered");
        }

        // Check if the password is valid
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Crear una política que solo permita texto plano (sin etiquetas HTML)
        PolicyFactory policy = new HtmlPolicyBuilder().toFactory();

        // Sanitizar title y description
        String safeUsername = policy.sanitize(username);
        String safeEmail = policy.sanitize(email);
        String safePassword = policy.sanitize(password);

        boolean isPicValid = validateProfilePic(profilePhoto);

        String encoded = passwordEncoder.encode(safePassword);

        String roles = "USER";

        // Create a new user
        User user = new User(safeUsername, safeEmail, encoded, null, roles);

        // If a profile photo is provided, convert it to a Blob and set it
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                Blob photoBlob = new SerialBlob(profilePhoto.getBytes());
                user.setProfilePicture(photoBlob);
            } catch (SQLException e) {
                throw new IOException("Error al convertir la imagen a Blob: " + e.getMessage());
            }
        } else {
            // Optional: Set a default profile picture or leave it as null
            // nothing to do here
            // Right now, we are not setting a default profile picture
        }

        // Store and return the user
        return userRepository.save(user);
    }

    /**
     * Authenticates a user using their username or email and password.
     */
    public Optional<User> authenticate(String emailOrUsername, String password) {
        Optional<User> userOptional = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println(
                    "Usuario encontrado: " + user.getUsername() + ", Contraseña en DB: " + user.getEncodedPassword()
                            + ", Contraseña introducida: " + password);
            if (user.getEncodedPassword().equals(password)) {
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

    public ResponseEntity<AuthResponse> signup(String username, String email, String password,
            MultipartFile profilePhoto, HttpServletResponse response) {
        try {
            // 1. Register the user
            User user = registerUser(username, email, password, profilePhoto);

            // 2. Authenticate the user
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,
                    password);
            Authentication auth = authManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 3. Generate JWT tokens
            LoginRequest loginRequest = new LoginRequest(username, password);
            return userService.login(response, loginRequest); // Reuse login logic

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(Status.FAILURE, "Registration error: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(Status.FAILURE, "Error uploading profile image"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(Status.FAILURE, "Unexpected error: " + e.getMessage()));
        }
    }

    public boolean validateProfilePic(MultipartFile imageFile) {
        // Si no hay ficheros, aceptamos
        if (imageFile == null || imageFile.isEmpty()) {
            return true;
        }

        // Si hay imagen, validarla
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageType = imageFile.getContentType();
            if (imageType == null || !imageType.startsWith("image/")) {
                throw new IllegalArgumentException("El archivo debe ser una imagen válida.");
            }
            if (imageFile.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("La imagen excede el tamaño máximo (5 MB).");
            }
        }

        return true;
    }
}
