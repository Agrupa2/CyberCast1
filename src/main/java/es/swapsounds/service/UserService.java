package es.swapsounds.service;

import es.swapsounds.DTO.UserDTO;
import es.swapsounds.DTO.UserMapper;
import es.swapsounds.DTO.UserRegistrationDTO;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.SoundRepository;
import es.swapsounds.repository.CommentRepository;
import es.swapsounds.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, SoundRepository soundRepository, UserMapper mapper,
            CommentRepository CommentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
        this.mapper = mapper;
        this.commentRepository = CommentRepository;
        this.passwordEncoder = passwordEncoder;

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be empty");
        }

        // Get the user performing the action (session)
        User sessionUser = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));

        // Get the target user (to modify)
        User targetUser = userRepository.findByUsername(pathUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check permissions: admin or profile owner
        boolean isAdmin = sessionUser.getRoles().contains("ADMIN");
        if (!isAdmin && sessionUser.getUserId() != targetUser.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this user");
        }

        // Update name
        targetUser.setUsername(newUsername.trim());
        userRepository.save(targetUser);
    }

    public void updateProfilePicture(long sessionUserId, long targetUserId, MultipartFile profilePhoto)
            throws IOException {
        // Get the user performing the action (session)
        User sessionUser = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));

        // Get the target user (to modify)
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check permissions: admin or profile owner
        boolean isAdmin = sessionUser.getRoles().contains("ADMIN");
        if (!isAdmin && sessionUser.getUserId() != targetUser.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this user");
        }

        // Update avatar
        try {
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                Blob photoBlob = new SerialBlob(profilePhoto.getBytes());
                targetUser.setProfilePicture(photoBlob);
            } else {
                targetUser.setProfilePicture(null);
            }
            userRepository.save(targetUser);
        } catch (SQLException e) {
            throw new RuntimeException("Error converting image to Blob: " + e.getMessage());
        }
    }

    public void updateAvatar(Long currentUserId, String targetUsername, MultipartFile file) {
        // 1. Validate current user
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        // 2. Get target user
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        // 3. Check permissions (admin or profile owner)
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        boolean isOwner = currentUser.getUserId() == targetUser.getUserId();

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this avatar");
        }

        // 4. Update avatar
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
                    "Error updating avatar: " + e.getMessage(),
                    e);
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
        // 1) Delete all comments written by the user
        commentRepository.deleteByUserUserId(userId);

        // 2) For each sound uploaded by the user:
        List<Sound> userSounds = soundRepository.findByUserId(userId);
        for (Sound sound : userSounds) {
            long sid = sound.getSoundId();
            // 2a) Delete comments pointing to that sound
            commentRepository.deleteBySoundId(sid);
            // 2b) Delete the sound
            soundRepository.deleteById(sid);
        }

        // 3) Delete the user
        userRepository.deleteById(userId);
    }

    public void deleteAccount(Long currentUserId, String targetUsername, String confirmation) {
        // 1. Validate confirmation
        if (!"ELIMINAR CUENTA".equals(confirmation != null ? confirmation.trim() : "")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You must confirm by typing 'ELIMINAR CUENTA'");
        }

        // 2. Get current user (who performs the action)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"));

        // 3. Get target user (to delete)
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Target user not found"));

        // 4. Check permissions (admin or account owner)
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        boolean isOwner = currentUser.getUserId() == targetUser.getUserId();

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to delete this account");
        }

        // 5. Delete related resources
        soundRepository.deleteAll(soundRepository.findByUserId(targetUser.getUserId()));

        // 6. Delete target user (not the current user)
        userRepository.deleteById(targetUser.getUserId());
    }

    public Map<String, Object> getProfileInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        String profileImageBase64 = null; // Change to null instead of ""
        String userInitial = null;
        boolean hasProfilePicture = false;

        Blob profilePicture = user.getProfilePicture();
        if (profilePicture != null) {
            try {
                byte[] imageBytes = profilePicture.getBytes(1, (int) profilePicture.length());
                profileImageBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
                hasProfilePicture = true;
            } catch (SQLException e) {
                System.err.println("Error converting Blob to Base64: " + e.getMessage());
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
        user.setEncodedPassword(passwordEncoder.encode(dto.password()));
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

}