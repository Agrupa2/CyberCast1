package es.swapsounds.service;

import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryStorage;
import es.swapsounds.storage.SoundRepository;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SoundService {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private InMemoryStorage storage;

    @Autowired
    private SoundRepository soundRepository;

    @PostConstruct
    private void initializeDefaultSounds() {

        Category football = categoryService.findOrCreateCategory("Football");
        Category meme = categoryService.findOrCreateCategory("Meme");

        sounds.add(new Sound(idCounter++, "Betis Anthem", "Relaxing forest ambiance", "/audio/betis.mp3",
                "/images/betis.png", Arrays.asList(football), "0:07"));
        sounds.add(new Sound(idCounter++, "CR7", "Soothing ocean waves", "/audio/CR7.mp3", "/images/CR7.jpg",
                Arrays.asList(football), "0:06"));
        sounds.add(new Sound(idCounter++, "El diablo que malditos tenis", "Peaceful rain for sleep",
                "/audio/el-diablo-que-malditos-tenis.mp3", "/images/el-diablo-que-malditos-tenis.png",
                Arrays.asList(meme), "0:04"));
    }

    private final List<Sound> sounds = new ArrayList<>();
    private long idCounter = 1;

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
        return soundRepository.save(sound);
    }

    public void deleteSound(long soundId) {
        soundRepository.deleteById(soundId);
    }

    public Sound updateSound(Sound sound) {
        return soundRepository.save(sound);
    }

    public String calculateDuration(MultipartFile audioFile) throws IOException {
        // Código de cálculo de duración. (Es posible que lo mantengas igual.)
        File audioTempFile = Files.createTempFile("audio", ".mp3").toFile();
        try (InputStream inputStream = audioFile.getInputStream()) {
            Files.copy(inputStream, audioTempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
        // 1. Calcular duración
        String duration = calculateDuration(audioFile);

        // 2. Guardar archivos
        String audioPath = storage.saveFile(user.getUsername(), audioFile, "sounds");
        String imagePath = storage.saveFile(user.getUsername(), imageFile, "images");

        // 3. Crear el objeto Sound sin asignar manualmente el ID (JPA lo generará)
        Sound sound = new Sound();
        sound.setTitle(title);
        sound.setDescription(description);
        sound.setFilePath(audioPath);
        sound.setImagePath(imagePath);
        sound.setUserId(user.getUserId());
        sound.setDuration(duration);
        sound.setUploadDate(LocalDateTime.now());
        sound.setCategories(new ArrayList<>());

        // 4. Procesar categorías
        for (String catName : categoryNames) {
            Category category = categoryService.findOrCreateCategory(catName);
            sound.addCategory(category);
            category.getSounds().add(sound);
        }

        // 5. Guardar el sonido en la base de datos
        return soundRepository.save(sound);
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
        if (audioFile != null && !audioFile.isEmpty()) {
            String newAudioPath = storage.saveFile(username, audioFile, "sounds");
            sound.setFilePath(newAudioPath);
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String newImagePath = storage.saveFile(username, imageFile, "images");
            sound.setImagePath(newImagePath);
        }

        // 5. Actualizar en la BD
        soundRepository.save(sound);
    }

}
