package es.swapsounds.service;

import es.swapsounds.model.User;
import es.swapsounds.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SecretSoundService {

    private static final String BASE_DIRECTORY = System.getProperty("user.dir");
    private static final String SECRET_DIRECTORY = "/uploads/secret-sounds/";
    private final Path secretBasePath;

    @Autowired
    private UserRepository userRepository;

    public SecretSoundService() {
        this.secretBasePath = Paths.get(BASE_DIRECTORY + SECRET_DIRECTORY)
                .toAbsolutePath()
                .normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(secretBasePath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo inicializar el directorio de sonidos secretos: " + secretBasePath, e);
        }
    }

    public ResponseEntity<Resource> downloadSecretSound(Long userId, Principal principal) {

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        // Obtener usuario actual desde el principal
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no autenticado"));

        // Verificar si es propietario o admin
        if (currentUser.getUserId() != targetUser.getUserId() && !currentUser.getRoles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a este recurso");
        }

        String secretPath = targetUser.getSecretPath();
        if (secretPath == null) {
            throw new NoSuchElementException("No existe sonido secreto para este usuario");
        }

        Path file = secretBasePath.resolve(secretPath).normalize();
        if (!file.startsWith(secretBasePath) || !Files.exists(file)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        }

        try {
            UrlResource resource = new UrlResource(file.toUri());
            String contentType = Files.probeContentType(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al leer el archivo", e);
        } catch (IOException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error obteniendo tipo de contenido", e);
        }
    }

    public void uploadSecretSound(Long userId, MultipartFile file, Principal principal) throws IOException {

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        // Obtener usuario actual desde el principal
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no autenticado"));

        // Verificar si es propietario o admin
        if (currentUser.getUserId() != targetUser.getUserId() && !currentUser.getRoles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a este recurso");
        } 

        String original = Paths.get(file.getOriginalFilename()).getFileName().toString();
        if (!original.matches("[a-zA-Z0-9._\\-() ]+\\.mp3")) {
            throw new IllegalArgumentException("Solo se permiten MP3 y nombre limpio");
        }
        if (!file.getContentType().startsWith("audio/")) {
            throw new IllegalArgumentException("Debe ser un audio válido");
        }

        String uuidName = UUID.randomUUID() + "-" + original;
        Path target = secretBasePath.resolve(uuidName).normalize();
        if (!target.startsWith(secretBasePath)) {
            throw new SecurityException("Ruta inválida detectada");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        targetUser.setSecretPath(uuidName);
        userRepository.save(targetUser);
    }

    public void deleteSecretSound(Long userId, Principal principal) {

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        // Obtener usuario actual desde el principal
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no autenticado"));

        // Verificar si es propietario o admin
        if (currentUser.getUserId() != targetUser.getUserId() && !currentUser.getRoles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a este recurso");
        }

        String secretPath = targetUser.getSecretPath();
        if (secretPath == null) {
            throw new NoSuchElementException("No existe sonido secreto para este usuario");
        }

        Path file = secretBasePath.resolve(secretPath).normalize();
        if (!file.startsWith(secretBasePath)) {
            throw new SecurityException("Ruta inválida detectada");
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo borrar el archivo", e);
        }

        targetUser.setSecretPath(null);
        userRepository.save(targetUser);
    }

    private void authorize(User user, Principal principal) {
        if (principal == null || !user.getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("No tienes permiso para esta operación");
        }
    }
}
