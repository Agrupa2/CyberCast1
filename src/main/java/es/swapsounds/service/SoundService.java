package es.swapsounds.service;

import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.storage.InMemoryStorage;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SoundService {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private InMemoryStorage storage;

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

    public List<Sound> getSoundByUserId(long soundId) {
        return sounds.stream()
                .filter(sound -> sound.getUserId() == soundId)
                .collect(Collectors.toList());
    }

    public List<Sound> getAllSounds() {
        return new ArrayList<>(sounds);
    }

    public Optional<Sound> findSoundById(long id) {
        return sounds.stream()
                .filter(s -> s.getSoundId() == id)
                .findFirst();
    }

    public void addSound(Sound sound) {
        sound.setSoundId(idCounter++);
        sounds.add(sound);
    }

    public void deleteSound(long soundId) {
        Optional<Sound> soundOptional = sounds.stream()
                .filter(s -> s.getSoundId() == soundId)
                .findFirst();

        if (soundOptional.isPresent()) {
            Sound sound = soundOptional.get();

            // Eliminar archivos físicos
            try {
                if (sound.getFilePath() != null) {
                    Path audioPath = Paths.get("uploads" + sound.getFilePath().replace("/uploads/", "/"));
                    Files.deleteIfExists(audioPath);
                }
                if (sound.getImagePath() != null) {
                    Path imagePath = Paths.get("uploads" + sound.getImagePath().replace("/uploads/", "/"));
                    Files.deleteIfExists(imagePath);
                }
            } catch (IOException e) {
                System.err.println("Error eliminando archivos de sonido: " + e.getMessage());
            }
            // Eliminar de la lista de sonidos
            sounds.remove(sound);
        }
    }

    public void updateSound(Sound updatedSound) {
        sounds.removeIf(s -> s.getSoundId() == updatedSound.getSoundId());
        sounds.add(updatedSound);
    }

    public String calculateDuration(MultipartFile audioFile) throws IOException {
        File audioTempFile = Files.createTempFile("audio", ".mp3").toFile();
        try (InputStream inputStream = audioFile.getInputStream()) {
            Files.copy(inputStream, audioTempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        String duration = "0"; // fallback por si algo falla
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

        // 1. Calcular la duración del audio
        String duration = calculateDuration(audioFile);

        // 2. Guardar archivos
        String audioPath = storage.saveFile(user.getUsername(), audioFile, "sounds");
        String imagePath = storage.saveFile(user.getUsername(), imageFile, "images");

        Sound sound = new Sound(0, title, description, audioPath, imagePath, user, new ArrayList<>(), duration);

        // 4. Procesar y asignar categorías
        for (String categoryName : categoryNames) {
            Category category = categoryService.findOrCreateCategory(categoryName);
            sound.getCategories().add(category);
            category.getSounds().add(sound); // Relación bidireccional
        }

        // 5. Asignar ID y almacenar el sonido en la lista (o repositorio)
        sound.setSoundId(idCounter++);

        return sound;
    }

    public List<Sound> getFilteredSounds(String query, String category) {
        List<Sound> allSounds = getAllSounds();
        return allSounds.stream().filter(sound -> {
            // Filtro de categoría
            boolean matchesCategory = true;
            if (!"all".equalsIgnoreCase(category)) {
                matchesCategory = sound.getCategories() != null && sound.getCategories().stream()
                        .anyMatch(cat -> cat.getName().equalsIgnoreCase(category));
            }
            // Filtro de búsqueda
            boolean matchesQuery = (query == null || query.trim().isEmpty()) ||
                    sound.getTitle().toLowerCase().contains(query.toLowerCase());
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

        Sound sound = findSoundById(soundId).orElseThrow(() -> new RuntimeException("Sonido no encontrado"));

        // 1. Limpiar relaciones de categorías anteriores
        if (sound.getCategories() != null) {
            for (Category category : sound.getCategories()) {
                category.getSounds().remove(sound);
            }
            sound.getCategories().clear();
        }

        // 2. Actualizar datos básicos
        sound.setTitle(title);
        sound.setDescription(description);

        // 3. Nuevas categorías
        for (String catName : categoryNames) {
            Category category = categoryService.findOrCreateCategory(catName);
            sound.addCategory(category); // agrega a sound
            category.getSounds().add(sound); // agrega a categoría
        }

        // 4. Archivos opcionales
        if (audioFile != null && !audioFile.isEmpty()) {
            String newAudioPath = storage.saveFile(username, audioFile, "sounds");
            sound.setFilePath(newAudioPath);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String newImagePath = storage.saveFile(username, imageFile, "images");
            sound.setImagePath(newImagePath);
        }
    }

}
