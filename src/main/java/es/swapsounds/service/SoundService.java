package es.swapsounds.service;

import es.swapsounds.model.Sound;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SoundService {

    public SoundService() {
        sounds.add(new Sound(idCounter++, "Betis Anthem", "Relaxing forest ambiance", "/audio/betis.mp3",
                "/images/betis.png", "Football", "0:07"));
        sounds.add(new Sound(idCounter++, "CR7", "Soothing ocean waves", "/audio/CR7.mp3", "/images/CR7.jpg",
                "Football", "0:06"));
        sounds.add(new Sound(idCounter++, "El diablo que malditos tenis", "Peaceful rain for sleep",
                "/audio/el-diablo-que-malditos-tenis.mp3", "/images/el-diablo-que-malditos-tenis.png", "Meme", "0:04"));
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

            // Eliminar de la lista de sonidos
            sounds.remove(sound);
        }
    }

    public void updateSound(Sound updatedSound) {
        sounds.removeIf(s -> s.getSoundId() == updatedSound.getSoundId());
        sounds.add(updatedSound);
    }

}
