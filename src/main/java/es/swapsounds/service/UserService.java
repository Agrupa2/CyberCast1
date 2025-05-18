package es.swapsounds.service;

import es.swapsounds.dto.UserDTO;
import es.swapsounds.dto.UserMapper;
import es.swapsounds.dto.UserRegistrationDTO;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.SoundRepository;
import es.swapsounds.repository.CommentRepository;
import es.swapsounds.repository.UserRepository;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, SoundRepository soundRepository, UserMapper mapper,
            CommentRepository CommentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
        this.mapper = mapper;
        this.commentRepository = CommentRepository;
        this.passwordEncoder = passwordEncoder;

    }

    public Optional<Long> getUserIdByUsername(String username) {
        return userRepository.findByUsername(username).map(User::getUserId);
    }

    public boolean isAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRoles().contains("ADMIN"))
                .orElse(false);
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public void updateUsername(long userId, String newUsername) {

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String cleanUsername = policy.sanitize(newUsername);

        userRepository.findById(userId).ifPresent(user -> {
            user.setUsername(cleanUsername);
            userRepository.save(user);
        });
    }

    public void changeUsername(Long sessionUserId, String pathUsername, String newUsername) {
        if (newUsername == null || newUsername.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede estar vacío");
        }

        // Obtener el usuario que realiza la acción (sesión)
        User sessionUser = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));

        // Obtener el usuario objetivo (a modificar)
        User targetUser = userRepository.findByUsername(pathUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Verificar permisos: admin o dueño del perfil
        boolean isAdmin = sessionUser.getRoles().contains("ADMIN");
        if (!isAdmin && sessionUser.getUserId() != targetUser.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar este usuario");
        }

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String cleanUsername = policy.sanitize(newUsername);
        // Actualizar nombre
        targetUser.setUsername(cleanUsername.trim());
        userRepository.save(targetUser);
    }

    public void updateProfilePicture(long sessionUserId, long targetUserId, MultipartFile profilePhoto)
            throws IOException {
        // Obtener el usuario que realiza la acción (sesión)
        User sessionUser = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));

        // Obtener el usuario objetivo (a modificar)
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Verificar permisos: admin o dueño del perfil
        boolean isAdmin = sessionUser.getRoles().contains("ADMIN");
        if (!isAdmin && sessionUser.getUserId() != targetUser.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar este usuario");
        }

        boolean checkProfilePic = validateProfilePic(profilePhoto);

        if (checkProfilePic) {
            try {
                if (profilePhoto != null && !profilePhoto.isEmpty()) {
                    Blob photoBlob = new SerialBlob(profilePhoto.getBytes());
                    targetUser.setProfilePicture(photoBlob);
                } else {
                    targetUser.setProfilePicture(null);
                }
                userRepository.save(targetUser);
            } catch (SQLException e) {
                throw new RuntimeException("Error al convertir la imagen a Blob: " + e.getMessage());
            }
        }
    }

    public void updateAvatar(Long currentUserId, String targetUsername, MultipartFile file) {
        // 1. Validar usuario actual
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        // 2. Obtener usuario objetivo
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario objetivo no encontrado"));

        // 3. Verificar permisos (admin o dueño del perfil)
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        boolean isOwner = currentUser.getUserId() == targetUser.getUserId();

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para modificar este avatar");
        }

        boolean checkProfilePic = validateProfilePic(file);

        if (checkProfilePic) {
            try {
                Blob blob = null;
                if (file != null && !file.isEmpty()) {
                    blob = new SerialBlob(file.getBytes());
                }
                targetUser.setProfilePicture(blob);
                userRepository.save(targetUser);

            } catch (SQLException | IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al actualizar el avatar: " + e.getMessage(),
                        e);
            }
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

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"));
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        if (isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes eliminar a un admin");
        } else {
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

    }

    public void deleteAccount(Long currentUserId, String targetUsername, String confirmation) {

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String cleanConfirmation = policy.sanitize(confirmation);
        // 1. Validar confirmación
        if (!"ELIMINAR CUENTA".equals(cleanConfirmation != null ? cleanConfirmation.trim() : "")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes confirmar escribiendo 'ELIMINAR CUENTA'");
        }

        // 2. Obtener usuario actual (quien realiza la acción)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"));

        // 3. Obtener usuario objetivo (a eliminar)
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario objetivo no encontrado"));

        // 4. Verificar permisos (admin o dueño de la cuenta)
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        boolean isOwner = currentUser.getUserId() == targetUser.getUserId();

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para eliminar esta cuenta");
        }

        // 5. Eliminar recursos relacionados
        soundRepository.deleteAll(soundRepository.findByUserId(targetUser.getUserId()));

        // 6. Eliminar usuario objetivo (no el usuario actual)
        userRepository.deleteById(targetUser.getUserId());
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
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String cleanUsername = policy.sanitize(dto.getUsername());
        String cleanEmail = policy.sanitize(dto.getEmail());
        String cleanPassword = policy.sanitize(dto.getPassword());

        User user = new User();
        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setEncodedPassword(passwordEncoder.encode(cleanPassword));
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

    public Long getUserIdFromPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findByUsername(username).map(User::getUserId).orElse(null);
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