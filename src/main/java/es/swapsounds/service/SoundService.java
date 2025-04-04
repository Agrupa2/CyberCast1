package es.swapsounds.service;

import es.swapsounds.model.Sound;
import es.swapsounds.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoundService {

    @Autowired
    private InMemoryStorage storage;

    public List<Sound> getSoundsByUserId(Long userId) {
        return storage.getSoundsByUserId(userId);
    }

    
    

    
}
