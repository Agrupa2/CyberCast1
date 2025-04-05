package es.swapsounds.service;

import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.storage.CommentRepository;
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
    private CommentRepository commentRepository;

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


    public List <Sound> getSoundByUserId(long soundId) {
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
        Optional <Sound> soundOptional = sounds.stream()
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

            commentRepository.deleteCommentsBySoundId(soundId);

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
}



