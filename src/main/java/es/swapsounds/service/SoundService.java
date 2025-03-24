package es.swapsounds.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.repository.SoundRepository;
import es.swapsounds.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import es.swapsounds.service.CategoryService;

@Service
public class SoundService {

    private final SoundRepository soundRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    @Autowired
    public SoundService(SoundRepository soundRepository,
                       UserRepository userRepository,
                       CategoryService categoryService) {
        this.soundRepository = soundRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public Sound uploadSound(String title, 
                            String description, 
                            Set<String> categories,
                            MultipartFile audioFile,
                            MultipartFile imageFile,
                            Long userId) throws IOException {

        // Validar usuario
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Crear el sonido
        Sound sound = new Sound();
        sound.setTitle(title);
        sound.setDescription(description);
        sound.setUser(user);

        // Convertir MultipartFile a byte[] y guardar en la entidad
        sound.setAudioFile(audioFile.getBytes());
        sound.setImageFile(imageFile.getBytes());

        // Procesar categorías, para que se añadan solo aquellas que exitan
        Set<Category> categorySet = categoryService.processCategories(categories);
        sound.setCategories(categorySet);

        // Guardar en la base de datos
        return soundRepository.save(sound);
    }

    @Transactional(readOnly = true)
    public Sound getSoundById(Long soundId) {
        return soundRepository.findById(soundId)
            .orElseThrow(() -> new EntityNotFoundException("Sonido no encontrado"));
    }

    @Transactional(readOnly = true)
    public byte[] getAudioFile(Long soundId) {
        Sound sound = getSoundById(soundId);
        return sound.getAudioFile();
    }

    @Transactional(readOnly = true)
    public byte[] getImageFile(Long soundId) {
        Sound sound = getSoundById(soundId);
        return sound.getImageFile();
    }

    @Transactional
    public Sound updateSound(Long soundId, 
                             String title, 
                             String description, 
                             Set<String> categories) {

        Sound sound = getSoundById(soundId);
        sound.setTitle(title);
        sound.setDescription(description);

        // Actualizar categorías
        if (!categories.isEmpty()) {
            Set<Category> categorySet = categoryService.processCategories(categories);
            sound.setCategories(categorySet);
        }

        return soundRepository.save(sound);
    }

    @Transactional
    public void deleteSound(Long soundId) {
        // Eliminar comentarios asociados (si existe CommentRepository)
        // commentRepository.deleteBySoundId(soundId);

        // Eliminar el sonido
        soundRepository.deleteById(soundId);
    }

    @Transactional(readOnly = true)
    public List<Sound> searchSounds(String query, List<String> categories) {
        if (!categories.isEmpty()) {
            return soundRepository.findByTitleContainingAndCategoriesNameIn(query, categories);
        }
        return soundRepository.findByTitleContaining(query);
    }

    @Transactional(readOnly = true)
    public List<Sound> getSoundsByUser(Long userId) {
        return soundRepository.findByUserId(userId);
    }
}
