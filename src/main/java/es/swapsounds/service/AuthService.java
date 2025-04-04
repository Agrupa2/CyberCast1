package es.swapsounds.service;

import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private InMemoryStorage storage;

    public User registerUser(String username, String email, String password, MultipartFile profilePhoto) throws IOException {
        if (storage.findUserByUsername(username).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }else if(storage.findUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        String photoPath = (profilePhoto != null && !profilePhoto.isEmpty())
                ? storage.saveFile(username, profilePhoto, "profiles")
                : "/uploads/profiles/default-avatar.png";

        User user = new User(username, email, password, photoPath);
        storage.addUser(user);

        return user;
    }

    public User authenticate(String username, String password) {
        Optional<User> user = storage.authenticate(username, password);
        return user.orElse(null);
    }

    
}
