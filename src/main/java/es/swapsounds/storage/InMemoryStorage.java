package es.swapsounds.storage;

import es.swapsounds.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class InMemoryStorage {
    private List<User> users = new ArrayList<>();
    private int idCounter = 1;

    public InMemoryStorage() {
        // Usuarios iniciales para pruebas
        users.add(new User("user", "user@gmail.com", "user123", "user.jpg", idCounter++, null));
        users.add(new User("admin", "admin@gmail.com", "admin123", "admin.jpg", idCounter++, null));
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

    public String saveProfilePhoto(String username, String fileName) {
        // Simulación: guarda la ruta en el sistema de archivos (por ejemplo, /uploads/)
        String uploadDir = "uploads/profiles/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        return uploadDir + username + "_" + fileName; // Ejemplo: uploads/profiles/user_profile.jpg
    }
}