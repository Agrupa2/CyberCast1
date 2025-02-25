package es.swapsounds.controller;

import es.swapsounds.model.Sound;
import es.swapsounds.model.User;
import es.swapsounds.service.UserSoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class UserController {

   @Autowired
   private UserSoundService userSoundService;

   @GetMapping("/dashboard")
   public String dashboard(Model model) {
      User user = getCurrentUser(); // Método para obtener el usuario actual

      if (user != null) {
         model.addAttribute("user", user);

         if ("admin".equals(user.getRole())) {
            model.addAttribute("isAdmin", true);
         } else {
            model.addAttribute("canUploadDownload", true);
            List<Sound> userSounds = userSoundService.getSoundsByUser(user.getUserId());
            model.addAttribute("userSounds", userSounds);
         }
      }

      return "UserDashboard";
   }

   @GetMapping("/loginUser")
   public String login(Model model) {
      User user = getCurrentUser(); // Método para obtener el usuario actual

      if (user != null) {
         return "redirect:/dashboard";
      }

      return "Login";
   }

   @PostMapping("/upload")
   public String uploadSound(@RequestParam("soundFile") MultipartFile soundFile,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam("soundTitle") String soundTitle,
                             Model model) throws IOException {
      User user = getCurrentUser(); // Método para obtener el usuario actual

      if (user != null) {
         // Guardar el archivo de sonido
         String soundFileName = soundFile.getOriginalFilename();
         File soundDest = new File("path/to/save/sounds/" + soundFileName);
         soundFile.transferTo(soundDest);

         // Guardar el archivo de imagen
         String imageFileName = imageFile.getOriginalFilename();
         File imageDest = new File("path/to/save/images/" + imageFileName);
         imageFile.transferTo(imageDest);

         // Crear y guardar el nuevo sonido
         Sound sound = new Sound();
         sound.setTitle(soundTitle);
         sound.setFilePath("/resource/static/audio/" + soundFileName);
         sound.setImagePath("/resource/static/images/" + imageFileName);
         sound.setId(user.getUserId());
         UserSoundService.saveSound(sound,user);
      }

      return "redirect:/start";
   }

   @PostMapping("/deleteSound")
   public String deleteSound(@RequestParam("soundId") int soundId, Model model) {
      User user = getCurrentUser(); // Método para obtener el usuario actual

      if (user != null) {
         Sound sound = UserSoundService.getSoundById(soundId);

         if (sound != null && (sound.getId() == user.getUserId() || "admin".equals(user.getRole()))) {
            UserSoundService.deleteSound(soundId);
         }
      }

      return "redirect:/dashboard";
   }

   private User getCurrentUser() {
      // Implementa la lógica para obtener el usuario actual
      return null;
   }
}
