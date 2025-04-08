package es.swapsounds.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.Sound;

public interface SoundRepository extends JpaRepository<Sound, Long> {
}