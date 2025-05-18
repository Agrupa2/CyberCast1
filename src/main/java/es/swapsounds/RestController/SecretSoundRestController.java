package es.swapsounds.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.swapsounds.service.SecretSoundService;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/secret-sounds")
public class SecretSoundRestController {

    private final SecretSoundService secretSoundService;
    private final UserService userService;

    public SecretSoundRestController(SecretSoundService secretSoundService, UserService userService) {
        this.secretSoundService = secretSoundService;
        this.userService = userService;
    }

    @PostMapping("/{username}/upload")
    public ResponseEntity<?> uploadSecretSound(
            @PathVariable String username,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            Long currentUserId = userService.getUserIdFromPrincipal(request.getUserPrincipal());
            secretSoundService.uploadSecretSound(currentUserId, file, request.getUserPrincipal());
            return ResponseEntity.ok(Map.of("success", "Secret sound uploaded successfully"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission for this action"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing the file"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteSecretSound(
            @PathVariable String username,
            HttpServletRequest request) {

        try {
            Long currentUserId = userService.getUserIdFromPrincipal(request.getUserPrincipal());
            secretSoundService.deleteSecretSound(currentUserId, request.getUserPrincipal());
            return ResponseEntity.ok(Map.of("success", "Secret sound deleted successfully"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission for this action"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting the file"));
        }
    }

    @GetMapping("/{username}/download")
    public ResponseEntity<Resource> downloadSecretSound(
            @PathVariable String username,
            HttpServletRequest request) {

        try {
            Long currentUserId = userService.getUserIdFromPrincipal(request.getUserPrincipal());
            return secretSoundService.downloadSecretSound(currentUserId, request.getUserPrincipal());
        } catch (AuthenticationException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
