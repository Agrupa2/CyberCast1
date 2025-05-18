package es.swapsounds.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.Sound;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {
    List<Sound> findByUserId(long userId);
    Page <Sound> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Sound> findByTitleContainingIgnoreCaseAndCategories_NameIgnoreCase(String title, String category, Pageable pageable);
}
