package es.swapsounds.RestController;

import java.io.IOException;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.swapsounds.dto.UserDTO;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService svc;

    public UserRestController(UserService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable p = PageRequest.of(page, size);
        Page<UserDTO> dtos = svc.findAllUsersDTO(p);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> get(@PathVariable String username) {
        UserDTO dto = svc.findByUsernameDTO(username);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{username}/username")
    public ResponseEntity<Map<String, String>> updateUsername(
            @PathVariable String username,
            @RequestParam String newUsername,
            HttpSession session) {
        Long id = svc.getUserIdFromSession(session);
        svc.changeUsername(id, username, newUsername);
        session.setAttribute("username", newUsername.trim());
        return ResponseEntity.ok(Map.of("success", "Nombre actualizado"));
    }

    @PostMapping("/{username}/avatar")
    public ResponseEntity<Map<String, String>> updateAvatar(
            @PathVariable String username,
            @RequestParam MultipartFile avatar,
            HttpSession session) throws IOException {
        Long id = svc.getUserIdFromSession(session);
        svc.updateProfilePicture(id, avatar);
        return ResponseEntity.ok(Map.of("success", "Avatar actualizado"));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String username,
            @RequestParam String confirmation,
            HttpSession session) {
        Long id = svc.getUserIdFromSession(session);
        svc.deleteAccount(id, confirmation);
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", "Cuenta eliminada"));
    }
}
