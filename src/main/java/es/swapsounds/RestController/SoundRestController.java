package es.swapsounds.RestController;

import es.swapsounds.DTO.SoundDTO;
import es.swapsounds.model.User;
import es.swapsounds.service.SoundService;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.rowset.serial.SerialException;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/sounds")
public class SoundRestController {

    private final SoundService svc;
    private final UserService usvc;

    public SoundRestController(SoundService svc, UserService usvc) {
        this.svc = svc;
        this.usvc = usvc;
    }

    @GetMapping
    public ResponseEntity<Page<SoundDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "all") String category) {
        
        Pageable p = PageRequest.of(page, size);
        Page<SoundDTO> dtos = svc.findAllSoundsDTO(p);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoundDTO> get(@PathVariable Long id) {
        SoundDTO dto = svc.findSoundByIdDTO(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, String>> create(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam List<String> categories,
            @RequestParam MultipartFile audioFile,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpSession session) throws IOException {

        Long id = usvc.getUserIdFromSession(session);
        Optional<User> user = usvc.getUserById(id);
        User userId = user.get();
        svc.createSound(title, description, categories, audioFile, imageFile, userId);
        return ResponseEntity.ok(Map.of("success", "Sonido creado exitosamente"));
    }

    @PostMapping("/{id}/audio")
    public ResponseEntity<Map<String, String>> updateAudio(
            @PathVariable Long id,
            @RequestParam("audioFile") MultipartFile audioFile,
            HttpSession session) throws IOException {

        Long userId = usvc.getUserIdFromSession(session);
        svc.updateAudio(id, audioFile, userId);
        return ResponseEntity.ok(Map.of("success", "Audio actualizado"));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Map<String, String>> updateImage(
            @PathVariable Long id,
            @RequestParam MultipartFile imageFile,
           HttpSession session) throws IOException, SerialException, SQLException {

        Long userId = usvc.getUserIdFromSession(session);
        svc.updateImage(id, imageFile, userId);
        return ResponseEntity.ok(Map.of("success", "Imagen actualizada"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            HttpSession session) {
        
        Long userId = usvc.getUserIdFromSession(session);
        svc.deleteSound(id, userId);
        return ResponseEntity.ok(Map.of("success", "Sonido eliminado"));
    }

    @GetMapping("/{id}/audio")
    public ResponseEntity<byte[]> getAudio(@PathVariable Long id) {
        byte[] audio = svc.getAudioContent(id)
                          .orElseThrow(() -> new IllegalArgumentException("Audio not found for ID: " + id));
        return ResponseEntity.ok()
                .header("Content-Type", "audio/mpeg")
                .body(audio);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] image = svc.getImageContent(id)
                          .orElseThrow(() -> new IllegalArgumentException("Image not found for ID: " + id));
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(image);
    }
}