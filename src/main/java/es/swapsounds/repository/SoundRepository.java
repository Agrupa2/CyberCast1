package es.swapsounds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.Sound;

public interface SoundRepository extends JpaRepository<Sound, Long> {
    List<Sound> findByUserId(Long userId);
    List<Sound> findByTitleContaining(String query);
    List<Sound> findByTitleContainingAndCategoriesNameIn(String query, List<String> categories);
}
