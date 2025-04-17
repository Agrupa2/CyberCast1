package es.swapsounds.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.Sound;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {
    List<Sound> findByUserId(long userId);
    List<Sound> findByCategories_NameIgnoreCase(String categoryName);
}
