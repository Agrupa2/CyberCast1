package es.swapsounds.service;

import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private InMemoryStorage storage;

    public Long getUserIdFromSession(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    public Optional<User> getUserById(Long userId) {
        return storage.findUserById(userId);
    }

    public void updateUsername(Long userId, String newUsername) {
        storage.updateUsername(userId, newUsername);
    }
}
