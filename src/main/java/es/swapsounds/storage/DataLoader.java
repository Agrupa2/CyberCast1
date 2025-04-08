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
            User user1 = new User();
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
            sound1.setTitle("CR7");
            sound1.setDescription("CR7 se va de los simpsons");
            sound1.setFilePath("/audio/CR7.mp3");
            sound1.setImagePath("/images/CR7.jpg");
            sound1.setDuration("3:15");
            sound1.setUploadDate(LocalDateTime.now());
            sound1.setUserId(user1.getUserId());

            Sound sound2 = new Sound();
            sound2.setTitle("Betis");
            sound2.setDescription("Betis anthem");
            sound2.setFilePath("/audio/betis.mp3");
            sound2.setImagePath("/images/betis.png");
            sound2.setDuration("5:00");
            sound2.setUploadDate(LocalDateTime.now());
            sound2.setUserId(user2.getUserId());

            Sound sound3 = new Sound();
            sound3.setTitle("El Diablo que malditos tenis");
            sound3.setDescription("Jolgorio ante a unos grandiosos tenis");
            sound3.setFilePath("/audio/el-diablo-que-malditos-tenis.mp3");
            sound3.setImagePath("/images/el-diablo-que-malditos-tenis.png");
            sound3.setDuration("3:00");
            sound3.setUploadDate(LocalDateTime.now());
            sound3.setUserId(user2.getUserId());

            Sound sound4 = new Sound();
            sound4.setTitle("El señor de la noche");
            sound4.setDescription("El señor de la noche, cabra");
            sound4.setFilePath("/audio/ElSenorDeLaNoche.mp3");
            sound4.setImagePath("/images/ElSenorDeLaNoche.jpg");
            sound4.setDuration("3:00");
            sound4.setUploadDate(LocalDateTime.now());
            sound4.setUserId(user1.getUserId());

            soundRepository.saveAll(List.of(sound1, sound2, sound3, sound4));

            System.out.println("Usuarios y sonidos de prueba cargados.");
        }
    }
}
