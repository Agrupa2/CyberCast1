package es.swapsounds.storage;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;

@Component
public class DataLoader implements CommandLineRunner {

    private final SoundRepository soundRepository;
    private final UserRepository userRepository;

    public DataLoader(SoundRepository soundRepository, UserRepository userRepository) {
        this.soundRepository = soundRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0 && soundRepository.count() == 0) {
            // Crear usuarios
            /* User user1 = new User();
            user1.setUsername("sofia");
            user1.setEmail("sofia@example.com");
            user1.setPassword("sofia123");
            user1.setProfilePicturePath(null); // o una imagen por defecto

            User user2 = new User();
            user2.setUsername("david");
            user2.setEmail("david@example.com");
            user2.setPassword("david123");
            user2.setProfilePicturePath(null);

            userRepository.saveAll(List.of(user1, user2));

            // Crear sonidos asociados a los usuarios
            Sound sound1 = new Sound();
            sound1.setTitle("Rain Sounds");
            sound1.setDescription("Relaxing rain for sleep");
            sound1.setFilePath("/sounds/rain.mp3");
            sound1.setImagePath("/images/rain.jpg");
            sound1.setDuration("3:15");
            sound1.setUploadDate(LocalDateTime.now());
            sound1.setUserId(user1.getUserId());

            Sound sound2 = new Sound();
            sound2.setTitle("Ocean Waves");
            sound2.setDescription("Calm ocean ambiance");
            sound2.setFilePath("/sounds/ocean.mp3");
            sound2.setImagePath("/images/ocean.jpg");
            sound2.setDuration("5:00");
            sound2.setUploadDate(LocalDateTime.now());
            sound2.setUserId(user2.getUserId());

            soundRepository.saveAll(List.of(sound1, sound2));

            System.out.println("Usuarios y sonidos de prueba cargados."); */
        }
    }
}
