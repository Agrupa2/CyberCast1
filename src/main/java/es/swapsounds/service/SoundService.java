package es.swapsounds.service;

import es.swapsounds.DTO.SoundDTO;
import es.swapsounds.DTO.SoundMapper;
import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.SoundRepository;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SoundRepository soundRepository;

    @Autowired
    private SoundMapper mapper;

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
            // 1. Calcular duración
            String duration = calculateDuration(audioFile);

            // 2. Convertir MultipartFile a Blob
            Blob audioBlob = new SerialBlob(audioFile.getBytes());
            Blob imageBlob = new SerialBlob(imageFile.getBytes());

            // 3. Crear el objeto Sound
            Sound sound = new Sound(title, description, audioBlob, imageBlob, user.getUserId(), new ArrayList<>(), duration);
            sound.setUploadDate(LocalDateTime.now());

            // 4. Procesar categorías
            for (String catName : categoryNames) {
                Category category = categoryService.findOrCreateCategory(catName);
                sound.addCategory(category);
                category.getSounds().add(sound);
            }

            // 5. Guardar el sonido en la base de datos
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
            // Filtro de categoría
            boolean matchesCategory = true;
            if (!"all".equalsIgnoreCase(category)) {
                matchesCategory = sound.getCategories() != null &&
                                  sound.getCategories().stream()
                                          .anyMatch(cat -> cat.getName().equalsIgnoreCase(category));
            }
            // Filtro de búsqueda
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

        // 1. Limpiar relaciones de categorías anteriores
        if (sound.getCategories() != null) {
            for (Category category : sound.getCategories()) {
                category.getSounds().remove(sound);
            }
            sound.getCategories().clear();
        }

        // 2. Actualizar campos básicos
        sound.setTitle(title);
        sound.setDescription(description);

        // 3. Procesar nuevas categorías
        for (String catName : categoryNames) {
            Category category = categoryService.findOrCreateCategory(catName);
            sound.addCategory(category);
            category.getSounds().add(sound);
        }

        // 4. Manejar archivos opcionales
        try {
            if (audioFile != null && !audioFile.isEmpty()) {
                Blob audioBlob = new SerialBlob(audioFile.getBytes());
                sound.setAudioBlob(audioBlob);
                // Recalcular la duración si se actualiza el audio
                sound.setDuration(calculateDuration(audioFile));
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                Blob imageBlob = new SerialBlob(imageFile.getBytes());
                sound.setImageBlob(imageBlob);
            }

            // 5. Actualizar en la BD
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
    // 1. Validar que el archivo no esté vacío
    if (audioFile == null || audioFile.isEmpty()) {
        throw new IllegalArgumentException("El archivo de audio no puede estar vacío");
    }

    // 2. Validar el tipo de archivo (opcional)
    String contentType = audioFile.getContentType();
    if (contentType == null || !contentType.startsWith("audio/")) {
        throw new IllegalArgumentException("Tipo de archivo no válido. Se esperaba un archivo de audio");
    }

    // 3. Buscar el sonido y validar propiedad
    Sound sound = soundRepository.findById(soundId)
        .orElseThrow(() -> new SoundNotFoundException(soundId));

    if (!Long.valueOf(userId).equals(sound.getUserId())) {
        throw new UnauthorizedAccessException("No tienes permiso para modificar este sonido");
    }

    // 4. Validar tamaño máximo (ejemplo: 10MB)
    long maxSize = 10 * 1024 * 1024; // 10MB
    if (audioFile.getSize() > maxSize) {
        throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (10MB)");
    }

    // 5. Convertir y guardar el audio
    try {
        byte[] audioBytes = audioFile.getBytes();
        Blob audioBlob = new SerialBlob(audioBytes);
        sound.setAudioBlob(audioBlob);
        
        soundRepository.save(sound);
        
    } catch (SQLException e) {
        throw new AudioProcessingException("Error al procesar el archivo de audio", e);
    }
}

public void updateImage(Long soundId, MultipartFile imageFile, Long userId) throws IOException, SerialException, SQLException {
    // Validate the sound exists
    Sound sound = soundRepository.findById(soundId)
            .orElseThrow(() -> new SoundNotFoundException(soundId));

    // Validate the user is authorized to update the sound
    if (!Long.valueOf(userId).equals(sound.getUserId())) {
        throw new UnauthorizedAccessException("User is not authorized to update this sound");
    }

    // Validate the image file size (example: 5MB max)
    long maxSize = 5 * 1024 * 1024; // 5MB
    if (imageFile.getSize() > maxSize) {
        throw new IllegalArgumentException("The image file exceeds the maximum allowed size (5MB)");
    }

    // Save the image file as a byte array or Blob
    byte[] imageBytes = imageFile.getBytes();
    sound.setImageBlob(new SerialBlob(imageBytes));

    // Save the updated sound entity
    soundRepository.save(sound);
}

public void deleteSound(Long id, Long userId) {
    // Validate the sound exists
    Sound sound = soundRepository.findById(id)
            .orElseThrow(() -> new SoundNotFoundException(id));

    // Validate the user is authorized to delete the sound
    if (!Long.valueOf(userId).equals(sound.getUserId())) {
        throw new UnauthorizedAccessException("User is not authorized to delete this sound");
    }

    // Delete the sound
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