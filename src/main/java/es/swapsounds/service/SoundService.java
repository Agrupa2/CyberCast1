package es.swapsounds.service;

import es.swapsounds.dto.SoundDTO;
import es.swapsounds.dto.SoundMapper;
import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.SoundRepository;
import es.swapsounds.repository.UserRepository;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.sql.Blob;
import java.sql.SQLException;

@Service
public class SoundService {

    private final SoundMapper mapper;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired

    public SoundService(@Qualifier("soundMapperImpl") SoundMapper mapper) {
        this.mapper = mapper;
    }

    private Long lastInsertedSoundId;

    public Long getLastInsertedSoundId() {
        return lastInsertedSoundId;
    }

    @PostConstruct
    private void initializeDefaultSounds() {
        if (soundRepository.count() == 0) {
        }
    }

    public List<Sound> getSoundByUserId(long userId) {
        return soundRepository.findByUserId(userId);
    }

    public List<Sound> getAllSounds() {
        return soundRepository.findAll();
    }

    public Optional<Sound> findSoundById(long id) {
        return soundRepository.findById(id);
    }

    public Sound addSound(Sound sound) {
        Sound savedSound = soundRepository.save(sound);
        this.lastInsertedSoundId = savedSound.getSoundId();
        return savedSound;
    }

    public void deleteSound(long soundId) {
        soundRepository.deleteById(soundId);
    }

    public Sound updateSound(Sound sound) {
        return soundRepository.save(sound);
    }

    public String calculateDuration(MultipartFile audioFile) throws IOException {
        File audioTempFile = Files.createTempFile("audio", ".mp3").toFile();
        try (InputStream inputStream = audioFile.getInputStream()) {
            Files.copy(inputStream, audioTempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        String duration = "00:00";
        try {
            Mp3File mp3file = new Mp3File(audioTempFile);
            long totalSeconds = mp3file.getLengthInSeconds();
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            duration = String.format("%02d:%02d", minutes, seconds);
        } catch (UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        } finally {
            audioTempFile.delete();
        }
        return duration;
    }

    public Sound createSound(String title,
            String description,
            List<String> categoryNames,
            MultipartFile audioFile,
            MultipartFile imageFile,
            User user) throws IOException {
        try {
            // 1. Calculate duration
            String duration = calculateDuration(audioFile);

            // 2. Convert MultipartFile to Blob
            Blob audioBlob = new SerialBlob(audioFile.getBytes());
            Blob imageBlob = new SerialBlob(imageFile.getBytes());

            // 3. Create a new Sound object
            Sound sound = new Sound(title, description, audioBlob, imageBlob, user.getUserId(), new ArrayList<>(),
                    duration);
            sound.setUploadDate(LocalDateTime.now());

            // 4. Process categories
            for (String catName : categoryNames) {
                Category category = categoryService.findOrCreateCategory(catName);
                sound.addCategory(category);
                category.getSounds().add(sound);
            }

            // 5. Store the sound in the database
            Sound savedSound = soundRepository.save(sound);
            this.lastInsertedSoundId = savedSound.getSoundId();
            return savedSound;

        } catch (SQLException e) {
            throw new IOException("Error al guardar los archivos en la base de datos", e);
        }
    }

    public List<Sound> getFilteredSounds(String query, String category) {
        List<Sound> allSounds = soundRepository.findAll();
        return allSounds.stream().filter(sound -> {
            // Filter by category
            boolean matchesCategory = true;
            if (!"all".equalsIgnoreCase(category)) {
                matchesCategory = sound.getCategories() != null &&
                        sound.getCategories().stream()
                                .anyMatch(cat -> cat.getName().equalsIgnoreCase(category));
            }
            // Filter by query
            boolean matchesQuery = (query == null || query.trim().isEmpty())
                    || sound.getTitle().toLowerCase().contains(query.toLowerCase());
            return matchesCategory && matchesQuery;
        }).collect(Collectors.toList());
    }

    public Set<String> getSelectedCategoryNames(Sound sound) {
        if (sound.getCategories() != null) {
            return sound.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public void editSound(
            long soundId,
            String title,
            String description,
            Set<String> categoryNames,
            MultipartFile audioFile,
            MultipartFile imageFile,
            String username) throws IOException {

        Sound sound = soundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Sonido no encontrado"));

        // 1. Clean up old categories
        if (sound.getCategories() != null) {
            for (Category category : sound.getCategories()) {
                category.getSounds().remove(sound);
            }
            sound.getCategories().clear();
        }

        // 2. Upload date the Database
        sound.setTitle(title);
        sound.setDescription(description);

        // 3. Process categories
        for (String catName : categoryNames) {
            Category category = categoryService.findOrCreateCategory(catName);
            sound.addCategory(category);
            category.getSounds().add(sound);
        }

        // 4. Manage audio and image files
        try {
            if (audioFile != null && !audioFile.isEmpty()) {
                Blob audioBlob = new SerialBlob(audioFile.getBytes());
                sound.setAudioBlob(audioBlob);
                // Recalculate duration
                sound.setDuration(calculateDuration(audioFile));
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                Blob imageBlob = new SerialBlob(imageFile.getBytes());
                sound.setImageBlob(imageBlob);
            }

            // 5. Upload date the Database
            soundRepository.save(sound);

        } catch (SQLException e) {
            throw new IOException("Error al actualizar los archivos en la base de datos", e);
        }
    }

    public Optional<byte[]> getAudioContent(Long soundId) {
        return soundRepository.findById(soundId)
                .map(sound -> {
                    try {
                        Blob audioBlob = sound.getAudioBlob();
                        return audioBlob != null ? audioBlob.getBinaryStream().readAllBytes() : null;
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException("Error al leer el audio", e);
                    }
                });
    }

    public Optional<byte[]> getImageContent(Long soundId) {
        return soundRepository.findById(soundId)
                .map(sound -> {
                    try {
                        Blob imageBlob = sound.getImageBlob();
                        return imageBlob != null ? imageBlob.getBinaryStream().readAllBytes() : null;
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException("Error al leer la imagen", e);
                    }
                });
    }

    public Page<SoundDTO> findAllSoundsDTO(Pageable page) {
        return soundRepository.findAll(page).map(mapper::toDTO);
    }

    public SoundDTO findSoundByIdDTO(Long id) {
        Sound sound = soundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sonido no encontrado"));
        return mapper.toDTO(sound);
    }

    public void updateAudio(Long soundId, MultipartFile audioFile, Long userId) throws IOException {
        // Validations
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("El archivo de audio no puede estar vacío");
        }

        String contentType = audioFile.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new IllegalArgumentException("Tipo de archivo no válido. Se esperaba un archivo de audio");
        }

        Sound sound = soundRepository.findById(soundId)
                .orElseThrow(() -> new SoundNotFoundException(soundId));

        // New: Check permissions (admin or sound owner)
        Optional<User> userOpt = userRepository.findById(userId);
        User user = userOpt.get();

        boolean isAdmin = user.getRoles().contains("ADMIN");
        if (!isAdmin && sound.getUserId() != userId) {
            throw new UnauthorizedAccessException("No tienes permiso para modificar este sonido");
        }

        // Other validations and logics
        long maxSize = 10 * 1024 * 1024;
        if (audioFile.getSize() > maxSize) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (10MB)");
        }

        try {
            byte[] audioBytes = audioFile.getBytes();
            Blob audioBlob = new SerialBlob(audioBytes);
            sound.setAudioBlob(audioBlob);
            soundRepository.save(sound);
        } catch (SQLException e) {
            throw new AudioProcessingException("Error al procesar el archivo de audio", e);
        }
    }

    public void updateImage(Long soundId, MultipartFile imageFile, Long userId)
            throws IOException, SerialException, SQLException {

        Sound sound = soundRepository.findById(soundId)
                .orElseThrow(() -> new SoundNotFoundException(soundId));

        // New: Check permissions
        Optional<User> userOpt = userRepository.findById(userId);
        User user = userOpt.get();

        boolean isAdmin = user.getRoles().contains("ADMIN");
        if (!isAdmin && sound.getUserId() != userId) {
            throw new UnauthorizedAccessException("Usuario no autorizado para actualizar este sonido");
        }

        // Other validations
        long maxSize = 5 * 1024 * 1024;
        if (imageFile.getSize() > maxSize) {
            throw new IllegalArgumentException("La imagen excede el tamaño máximo permitido (5MB)");
        }

        byte[] imageBytes = imageFile.getBytes();
        sound.setImageBlob(new SerialBlob(imageBytes));
        soundRepository.save(sound);
    }

    public void deleteSound(Long id, Long userId) {
        Sound sound = soundRepository.findById(id)
                .orElseThrow(() -> new SoundNotFoundException(id));

        // New: Check permissions
        Optional<User> userOpt = userRepository.findById(userId);
        User user = userOpt.get();

        boolean isAdmin = user.getRoles().contains("ADMIN");
        if (!isAdmin && sound.getUserId() != userId) {
            throw new UnauthorizedAccessException("Usuario no autorizado para eliminar este sonido");
        }

        soundRepository.delete(sound);
    }

    public class SoundNotFoundException extends RuntimeException {
        public SoundNotFoundException(Long soundId) {
            super("Spundido no encontrado con ID: " + soundId);
        }
    }

    public class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }

    public class AudioProcessingException extends RuntimeException {
        public AudioProcessingException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}