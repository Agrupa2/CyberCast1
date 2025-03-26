package es.swapsounds.repository;

import es.swapsounds.model.Sound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoundRepository extends JpaRepository <Sound, Integer> {

    
} 
