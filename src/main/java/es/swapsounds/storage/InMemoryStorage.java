package es.swapsounds.storage;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InMemoryStorage {
    private List<User> users = new ArrayList<>();
    private List<Sound> sounds = new ArrayList<>();
    private int idCounter = 1;

    public InMemoryStorage() {
        // Usuarios iniciales para pruebas
        users.add(new User("user", "user@gmail.com", "user123", null, idCounter++, null));
        users.add(new User("admin", "admin@gmail.com", "admin123", null, idCounter++, null));

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

    public String saveProfileImage(MultipartFile file, String username) throws IOException {
        String uploadDir = "uploads/profiles/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = username + "_" + System.currentTimeMillis() + ".jpg";
        Path filePath = Paths.get(uploadDir + fileName);

        file.transferTo(filePath);
        return "/uploads/profiles/" + fileName;
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
    public Sound getSoundById(int soundId) {
        return sounds.stream()
                .filter(sound -> sound.getId() == soundId)
                .findFirst()
                .orElse(null);
    }

    public void updateSound(Sound updatedSound) {
        // Implementación específica de tu almacenamiento
        sounds.removeIf(s -> s.getId() == updatedSound.getId());
        sounds.add(updatedSound);
    }

    public List<Sound> getSoundsByUserId(int userId) {
        return sounds.stream()
                .filter(sound -> sound.getUserId() == userId)
                .sorted(Comparator.comparing(Sound::getUploadDate).reversed())
                .collect(Collectors.toList());
    }

    // Añade estos métodos en la clase InMemoryStorage

    public void deleteUser(int userId) {
        // Eliminar usuario
        users.removeIf(u -> u.getUserId() == userId);
        
        // Eliminar sus sonidos
        List<Sound> userSounds = sounds.stream()
            .filter(s -> s.getUserId() == userId)
            .collect(Collectors.toList());
            
        userSounds.forEach(sound -> {
            // Eliminar archivos físicos
            try {
                Path audioPath = Paths.get("uploads" + sound.getFilePath().replace("/uploads/", "/"));
                Files.deleteIfExists(audioPath);
                
                Path imagePath = Paths.get("uploads" + sound.getImagePath().replace("/uploads/", "/"));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Error eliminando archivos: " + e.getMessage());
            }
        });
        sounds.removeAll(userSounds);
        
        // Eliminar imagen de perfil
        users.stream()
            .filter(u -> u.getUserId() == userId)
            .findFirst()
            .ifPresent(u -> {
                if (u.getProfilePicturePath() != null && !u.getProfilePicturePath().contains("default")) {
                    try {
                        Files.deleteIfExists(Paths.get("uploads" + u.getProfilePicturePath().replace("/uploads/", "/")));
                    } catch (IOException e) {
                        System.err.println("Error eliminando avatar: " + e.getMessage());
                    }
                }
            });
    }
}
