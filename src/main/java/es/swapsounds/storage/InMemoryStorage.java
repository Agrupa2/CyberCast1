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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InMemoryStorage {
    private List<User> users = new ArrayList<>();
    private List<Sound> sounds = new ArrayList<>();
    private long idCounter = 1;


    public InMemoryStorage() {
        // Locally generated users for testing
        users.add(new User("user", "user@gmail.com", "user123", null, idCounter++, null));
<<<<<<< HEAD
        users.add(new User("admin", "admin@gmail.com", "admin123", "https://imgs.search.brave.com/VuBfiTd2u6sg7kwHVE-LzZGF_uwTzV8Hssy42MikWg8/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9yZXNv/dXJjZXMudGlkYWwu/Y29tL2ltYWdlcy8w/MTRlYWYzMi84NjY5/LzRmYTkvYWRiZi8z/ODRjZmUzMjRmZTYv/NjQweDY0MC5qcGc", idCounter++, null));

        sounds.add(new Sound(idCounter++, "Betis Anthem", "Relaxing forest ambiance", "/audio/betis.mp3",
                "/images/betis.png", "Football", "0:07"));
        sounds.add(new Sound(idCounter++, "CR7", "Soothing ocean waves", "/audio/CR7.mp3", "/images/CR7.jpg",
                "Football", "0:06"));
        sounds.add(new Sound(idCounter++, "El diablo que malditos tenis", "Peaceful rain for sleep",
                "/audio/el-diablo-que-malditos-tenis.mp3", "/images/el-diablo-que-malditos-tenis.png", "Meme", "0:04"));

        initializeDefaultCategories();

    }

    private void initializeDefaultCategories() {
        addCategoryIfNotExists("Música");
        addCategoryIfNotExists("Podcast");
        addCategoryIfNotExists("Efectos de sonido");
        addCategoryIfNotExists("Naturaleza");
        addCategoryIfNotExists("Tecnología");
    }

    private void addCategoryIfNotExists(String name) {
        String normalizedName = name.trim().toLowerCase();
        boolean exists = categories.stream()
            .anyMatch(c -> c.getName().trim().equalsIgnoreCase(normalizedName));
=======
        users.add(new User("admin", "admin@gmail.com", "admin123", null, idCounter++, null));
>>>>>>> cfc36244caa45686adf7d26f9c567758b0319115
        
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
                .filter(u -> u.getEmail().equals(email)
                        || u.getUsername().equals(email) && u.getPassword().equals(password))
                .findFirst();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }


    public Optional<User> findUserById(long userId) {
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

        // Generates an unique file name
        String fileName = username + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        // Stores the file locally
        file.transferTo(new java.io.File(filePath));

        // Retourns the path to the file
        return "/uploads/" + directory + "/" + fileName;
    }

    public void deleteUser(long userId) {
        // Deletes the user from the list
        users.removeIf(u -> u.getUserId() == userId);

        // Deleting all the sounds of the user
        List<Sound> userSounds = sounds.stream()
                .filter(s -> s.getUserId() == userId)
                .collect(Collectors.toList());

        userSounds.forEach(sound -> {
            // Deletes the files stored locally
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

        // Profile Image deletion
        users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .ifPresent(u -> {
                    if (u.getProfilePicturePath() != null && !u.getProfilePicturePath().contains("default")) {
                        try {
                            Files.deleteIfExists(
                                    Paths.get("uploads" + u.getProfilePicturePath().replace("/uploads/", "/")));
                        } catch (IOException e) {
                            System.err.println("Error eliminando avatar: " + e.getMessage());
                        }
                    }
                });
    }

    public void updateUsername(long userId, String newUsername) {
        users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .ifPresent(u -> u.setUsername(newUsername));
    }

    public void updateProfilePicture(long userId, String imagePath) {
        users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .ifPresent(u -> u.setProfilePicturePath(imagePath));
    }

}