package es.swapsounds.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.SoundRepository;
import es.swapsounds.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SoundRepository soundRepository;


    public void deleteUser (Long userId){

        // Deleting all the sounds of the user
        this.soundRepository.deleteByUserId(userId);
        // It also deletes the profile Image...
        userRepository.deleteById(userId);

    }

    public Optional<User> findUserById(int userId) {
        return userRepository.findById((long)userId);
    }

    public boolean existsByUsername(String username) {
        return this.userRepository.findByUsername(username).isPresent();
    }

    public User createNewUser (String username, String email, String user_password, MultipartFile profile_photo) {

        // Check if the user uploaded a profile photo
        String photoPath = null;
        if (profile_photo != null && !profile_photo.isEmpty()) {
            try {
                photoPath = storage.saveFile(username, profile_photo, "profiles");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al subir la imagen de perfil");
                return "redirect:/signup";
            }
        } else {
            // Asign default profile photo
            photoPath = "/uploads/profiles/default-avatar.png";
        }

        User user = new User(username, email, user_password, photoPath);

        storage.addUser(user);
    }

    public Optional <User> authenticate(String emailOrUsername, String password) {
        return userRepository.findByEmailOrUsernameAndPassword(emailOrUsername, emailOrUsername, password);
    }

    @Transactional //f something fails during the execution no changes will be saved to the database.
    public void updateUsername(int userId, String newUsername) {
        userRepository.findById((long)userId).ifPresent(user -> {
            user.setUsername(newUsername);
            userRepository.save(user); // Keeps the changes
        });
    }



//
//
//    private List<User> users;
//
//    public UserService() {
//        this.users = new ArrayList<>();
//    }
//
//    public void registerUser(User user) {
//        users.add(user);
//    }
//
//    public void removeUser(User user) {
//        users.remove(user);
//    }
//
//    public List<User> getUsers() {
//        return users;
//    }
//
//    public User getUserByEmail(String email) {
//        for (User user : users) {
//            if (user.getEmail().equalsIgnoreCase(email)) {
//                return user;
//            }
//        }
//        return null;
//    }
}
