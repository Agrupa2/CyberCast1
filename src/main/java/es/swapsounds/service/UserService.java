package es.swapsounds.service;

import es.swapsounds.dto.UserDTO;
import es.swapsounds.dto.UserMapper;
import es.swapsounds.dto.UserRegistrationDTO;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.SoundRepository;
import es.swapsounds.repository.CommentRepository;
import es.swapsounds.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.Principal;
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
    private final CommentRepository commentRepository;
    private final UserMapper mapper;

    public UserService(UserRepository userRepository, SoundRepository soundRepository, UserMapper mapper, CommentRepository CommentRepository) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
        this.mapper = mapper;
        this.commentRepository = CommentRepository;

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

    public void changeUsername(Long sessionUserId, String pathUsername, String newUsername) {
        if (newUsername == null || newUsername.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede estar vacío");
        }

        User u = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "No autenticado"));

        if (!u.getUsername().equals(pathUsername)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar este usuario");
        }

        u.setUsername(newUsername.trim());
        userRepository.save(u);
    }

    public void updateProfilePicture(long userId, MultipartFile profilePhoto) throws IOException {
        userRepository.findById(userId).ifPresent(user -> {
            try {
                if (profilePhoto != null && !profilePhoto.isEmpty()) {
                    // Convertir el MultipartFile a Blob
                    Blob photoBlob = new SerialBlob(profilePhoto.getBytes());
                    user.setProfilePicture(photoBlob); // Asume que el campo en User es 'profilePicture' de tipo Blob
                } else {
                    // Si no se proporciona imagen, asignamos null
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

    public void updateAvatar(Long userId, String pathUsername, MultipartFile file) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        User u = userRepository.findById(userId)
            .orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
            );

        if (!u.getUsername().equals(pathUsername)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "No tienes permiso para modificar este avatar"
            );
        }

        try {
            Blob blob = (file != null && !file.isEmpty())
                ? new SerialBlob(file.getBytes())
                : null;
            u.setProfilePicture(blob);
            userRepository.save(u);
        } catch (SQLException | IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al subir avatar",
                e
            );
        }
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserDTO findByUsernameDTO(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return mapper.toDto(u);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<UserDTO> findAllUsersDTO(Pageable page) {
        return userRepository.findAll(page)
                .map(mapper::toDto);
    }

    public Optional<User> findUserById(long userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public void deleteUser(long userId) {
        // 1) Eliminar todos los comentarios que el usuario ha escrito
        commentRepository.deleteByUserUserId(userId);

        // 2) Para cada sonido que el usuario ha subido:
        List<Sound> userSounds = soundRepository.findByUserId(userId);
        for (Sound sound : userSounds) {
            long sid = sound.getSoundId();
            // 2a) Borrar comentarios apuntando a ese sonido
            commentRepository.deleteBySoundId(sid);
            // 2b) Borrar el sonido
            soundRepository.deleteById(sid);
        }

        // 3) Borrar al usuario
        userRepository.deleteById(userId);
    }

    public void deleteAccount(Long sessionUserId, String pathUsername, String confirmation) {
    // 1. Autenticación
    if (sessionUserId == null) {
        throw new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "No autenticado"
        );
    }

    // 2. Confirmación textual
    if (confirmation == null || !"ELIMINAR CUENTA".equals(confirmation.trim())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Debes confirmar escribiendo 'ELIMINAR CUENTA'"
        );
    }

    // 3. Obtener usuario y verificar autorización
    User u = userRepository.findById(sessionUserId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Usuario no encontrado"
        ));

    if (!u.getUsername().equals(pathUsername)) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No tienes permiso para eliminar esta cuenta"
        );
    }

    // 4. Borrado de sonidos y usuario
    soundRepository.deleteAll(soundRepository.findByUserId(sessionUserId));
    userRepository.deleteById(sessionUserId);
}


    public Map<String, Object> getProfileInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        String profileImageBase64 = null; // Cambia a null en lugar de ""
        String userInitial = null;
        boolean hasProfilePicture = false;

        Blob profilePicture = user.getProfilePicture();
        if (profilePicture != null) {
            try {
                byte[] imageBytes = profilePicture.getBytes(1, (int) profilePicture.length());
                profileImageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
                hasProfilePicture = true;
            } catch (SQLException e) {
                System.err.println("Error al convertir el Blob a Base64: " + e.getMessage());
            }
        }

        if (!hasProfilePicture) {
            userInitial = (user.getUsername() != null && !user.getUsername().isEmpty())
                    ? user.getUsername().substring(0, 1).toUpperCase()
                    : "?";
        }

        info.put("profileImageBase64", profileImageBase64);
        info.put("userInitial", userInitial);
        info.put("hasProfilePicture", hasProfilePicture);
        return info;
    }

    public UserDTO saveDTO(UserRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setEncodedPassword(dto.password());
        user.setRoles(List.of("ROLE_USER"));

        return mapper.toDto(userRepository.save(user));
    }


	public UserDTO getUser(String username) {
		return mapper.toDto(userRepository.findByUsername(username).orElseThrow());
	}

	public User getLoggedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).get();
    }

	public UserDTO getLoggedUserDTO() {
        return mapper.toDto(getLoggedUser());
    }

    public Optional<User> getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            return Optional.empty();
        }
        String username = principal.getName();
        return userRepository.findByUsername(username);
    }

}