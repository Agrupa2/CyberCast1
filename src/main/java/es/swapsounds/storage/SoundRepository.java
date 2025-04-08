package es.swapsounds.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.Sound;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {
    List <Sound> findByUserId(Long userId);
}
