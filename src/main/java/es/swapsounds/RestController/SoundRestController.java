package es.swapsounds.RestController;

import es.swapsounds.dto.SoundDTO;
import es.swapsounds.model.User;
import es.swapsounds.service.SoundService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.sql.rowset.serial.SerialException;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/sounds")
public class SoundRestController {

    private final SoundService svc;

    public SoundRestController(SoundService svc) {
        this.svc = svc;
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

    @PostMapping
    public ResponseEntity<Map<String, String>> create(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam List<String> categories,
            @RequestParam MultipartFile audioFile,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestHeader("X-User-Id") User userId) throws IOException {
        
        svc.createSound(title, description, categories, audioFile, imageFile, userId);
        return ResponseEntity.ok(Map.of("success", "Sonido creado exitosamente"));
    }

    @PostMapping("/{id}/audio")
    public ResponseEntity<Map<String, String>> updateAudio(
            @PathVariable Long id,
            @RequestParam MultipartFile audioFile,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        
        svc.updateAudio(id, audioFile, userId);
        return ResponseEntity.ok(Map.of("success", "Audio actualizado"));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Map<String, String>> updateImage(
            @PathVariable Long id,
            @RequestParam MultipartFile imageFile,
            @RequestHeader("X-User-Id") Long userId) throws IOException, SerialException, SQLException {

        svc.updateImage(id, imageFile, userId);
        return ResponseEntity.ok(Map.of("success", "Imagen actualizada"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        
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