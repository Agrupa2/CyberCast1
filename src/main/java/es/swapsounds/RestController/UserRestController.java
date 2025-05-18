package es.swapsounds.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.swapsounds.dto.UserDTO;
import es.swapsounds.dto.UserRegistrationDTO;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService svc;

    public UserRestController(UserService svc) {
        this.svc = svc;
    }


    @GetMapping
    public ResponseEntity<Page<UserDTO>> list(Pageable pageable) {
        Page<UserDTO> dtos = svc.findAllUsersDTO(pageable);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserRegistrationDTO dto) {
        UserDTO created = svc.saveDTO(dto);
        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(created.userId()).toUri();
        return ResponseEntity.created(location).body(created);
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
        svc.updateAvatar(id, username, avatar);
        return ResponseEntity.ok(Map.of("success", "Avatar actualizado"));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String username,
            @RequestParam String confirmation,
            HttpSession session) {
        Long id = svc.getUserIdFromSession(session);
        svc.deleteAccount(id, username, confirmation);
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", "Cuenta eliminada"));
    }
}
