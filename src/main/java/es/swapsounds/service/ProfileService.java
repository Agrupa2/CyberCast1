package es.swapsounds.service;

import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ProfileService {

    @Autowired
    private InMemoryStorage storage;

    public String getUserInitial(User user) {
        String profileImagePath = user.getProfilePicturePath();
        if (profileImagePath == null) {
            return user.getUsername().substring(0, 1).toUpperCase();
        }
        return "";
    }

    public void updateProfilePicture(Long userId, MultipartFile file) throws IOException {
        String uploadDir = "uploads/profiles/";
        Files.createDirectories(Paths.get(uploadDir));

        String filename = userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + filename);
        file.transferTo(filePath);

        String filePathStr = "/" + uploadDir + filename;
        storage.updateProfilePicture(userId, filePathStr);
    }
}
