package es.swapsounds.RestController;

import es.swapsounds.dto.SoundDTO;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.service.SoundService;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ResponseEntity<Map<String, Object>> page(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Sound> soundPage = svc.getFilteredSoundsPage(query, category, page, size);

        // Opcional: mapear a DTO si no quieres exponer toda la entidad
        List<SoundDTO> dtoList = soundPage.getContent().stream()
            .map(SoundDTO::new)
            .collect(Collectors.toList());

        Map<String,Object> resp = new HashMap<>();
        resp.put("sounds", dtoList);
        resp.put("currentPage", soundPage.getNumber());
        resp.put("totalPages", soundPage.getTotalPages());
        resp.put("hasNext", soundPage.hasNext());
        return ResponseEntity.ok(resp);
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
            @RequestParam MultipartFile imageFile,
            HttpServletRequest request) throws IOException {

        Long id = usvc.getUserIdFromPrincipal(request.getUserPrincipal());
        Optional<User> user = usvc.getUserById(id);
        User userId = user.get();
        svc.createSound(title, description, categories, audioFile, imageFile, userId);
        return ResponseEntity.ok(Map.of("success", "Sonido creado exitosamente"));
    }

    @PostMapping("/{id}/audio")
    public ResponseEntity<Map<String, String>> updateAudio(
            @PathVariable Long id,
            @RequestParam("audioFile") MultipartFile audioFile,
            HttpServletRequest request) throws IOException {

        Long userId = usvc.getUserIdFromPrincipal(request.getUserPrincipal());
        svc.updateAudio(id, audioFile, userId);
        return ResponseEntity.ok(Map.of("success", "Audio actualizado"));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Map<String, String>> updateImage(
            @PathVariable Long id,
            @RequestParam MultipartFile imageFile,
           HttpServletRequest request) throws IOException, SerialException, SQLException {

        Long userId = usvc.getUserIdFromPrincipal(request.getUserPrincipal());
        svc.updateImage(id, imageFile, userId);
        return ResponseEntity.ok(Map.of("success", "Imagen actualizada"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long userId = usvc.getUserIdFromPrincipal(request.getUserPrincipal());
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