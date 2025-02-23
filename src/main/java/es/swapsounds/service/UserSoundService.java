package es.swapsounds.service;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSoundService {

   public List<Sound> getSoundsByUser(int userId) {
      // Implementa la lógica para obtener los sonidos subidos por el usuario
      return null;
   }

   public static Sound getSoundById(int soundId) {
      // Implementa la lógica para obtener un sonido por su ID
      return null;
   }

   public static int deleteSound(int soundId) {
      return 0;
   }

   public static void saveSound(Sound sound, User user) {
      user.setSound(sound);
   }
}
