package es.swapsounds.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.Sound;

public interface SoundRepository extends JpaRepository<Sound, Long> {
    List<Sound> findByUserId(long userId);
}