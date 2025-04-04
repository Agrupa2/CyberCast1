package es.swapsounds.storage;

import es.swapsounds.model.User;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class InMemoryStorage {
    private List<User> users = new ArrayList<>();
    private long idCounter = 1;


    public InMemoryStorage() {
        // Locally generated users for testing
        users.add(new User("user", "user@gmail.com", "user123", null, idCounter++, null));
        users.add(new User("admin", "admin@gmail.com", "admin123", null, idCounter++, null));
        
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


}