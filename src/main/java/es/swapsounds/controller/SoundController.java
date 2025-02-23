package es.swapsounds.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import es.swapsounds.model.Sound;

@Controller
public class SoundController {

    private final List<Sound> sounds = List.of(
            new Sound(1, "Betis Anthem", "Relaxing forest ambiance", "/audio/betis.mp3", "/images/betis.png", "Football", "0:07"),
            new Sound(2, "CR7", "Soothing ocean waves", "/audio/CR7.mp3", "/images/CR7.jpg", "Football", "0:06"),
            new Sound(3, "El diablo que malditos tenis", "Peaceful rain for sleep", "/audio/el-diablo-que-malditos-tenis.mp3", "images/el-diablo-que-malditos-tenis.png", "Meme", "0:04"));

    @GetMapping("/start")
    public String showSounds(Model model) {
        model.addAttribute("sounds", sounds);
        return "start"; // Renderiza start.html
    }
}