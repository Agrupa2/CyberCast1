package es.swapsounds.storage;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class InMemoryStorage {
    private List<User> users = new ArrayList<>();
    private List<Sound> sounds = new ArrayList<>();
    private int idCounter = 1;

    public InMemoryStorage() {
        // Usuarios iniciales para pruebas
        users.add(new User("user", "user@gmail.com", "user123", "user.jpg", idCounter++, null));
        users.add(new User("admin", "admin@gmail.com", "admin123", "admin.jpg", idCounter++, null));

        sounds.add(new Sound(idCounter++, "Betis Anthem", "Relaxing forest ambiance", "/audio/betis.mp3", "/images/betis.png", "Football", "0:07"));
        sounds.add(new Sound(idCounter++, "CR7", "Soothing ocean waves", "/audio/CR7.mp3", "/images/CR7.jpg", "Football", "0:06"));
        sounds.add(new Sound(idCounter++, "El diablo que malditos tenis", "Peaceful rain for sleep", "/audio/el-diablo-que-malditos-tenis.mp3", "images/el-diablo-que-malditos-tenis.png", "Meme", "0:04"));
    
    }

    public void addUser(User user) {
        user.setId(idCounter++);
        users.add(user);
    }

    public Optional<User> findUserByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    public Optional<User> findUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public Optional<User> authenticate(String email, String password) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email) || u.getUsername().equals(email) && u.getPassword().equals(password))
                .findFirst();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public void addSound(Sound sound) {
        sound.setId(idCounter++);
        sounds.add(sound);
    }

    public List<Sound> getAllSounds() {
        return new ArrayList<>(sounds != null ? sounds : new ArrayList<>());
    }

    public Optional<Sound> findSoundById(int id) {
        return sounds.stream()
                .filter(s -> s.getId() == id)
                .findFirst();
    }

    public Optional<User> findUserById(int userId) {
        return users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst();
    }

    public String saveProfilePhoto(String username, String fileName) {
        // Simulación: guarda la ruta en el sistema de archivos (por ejemplo, /uploads/)
        String uploadDir = "uploads/profiles/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        return uploadDir + username + "_" + fileName; // Ejemplo: uploads/profiles/user_profile.jpg
    }

    public String saveFile(String username, MultipartFile file, String directory) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads/" + directory + "/";
        java.io.File dir = new java.io.File(uploadDir);
        
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + uploadDir);
            }
        }
    
        // Genera un nombre único para el archivo (ej: "user123_imagen.jpg")
        String fileName = username + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;
        
        // Guarda el archivo físicamente
        file.transferTo(new java.io.File(filePath));
        
        // Retorna la ruta relativa para la web (ej: "/uploads/sounds/user123_imagen.jpg")
        return "/uploads/" + directory + "/" + fileName; 
    }
<<<<<<< HEAD
}
    
=======

    public Sound getSoundById(int soundId) {
        return sounds.stream()
                .filter(sound -> sound.getId() == soundId)
                .findFirst()
                .orElse(null);
    }
}
>>>>>>> a43fe4d21fed99d0e4b6911aefec1824851ff366
