package es.swapsounds.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import es.swapsounds.model.Sound;

@Controller
public class SoundController {

    private final List<Sound> sounds = List.of(
        new Sound(1, "Nature Sound", "Relaxing forest ambiance", "/audio/nature.mp3", "/images/nature.jpg"),
        new Sound(2, "Ocean waves", "Soothing ocean waves", "/audio/ocean.mp3", "/images/ocean.jpg"),
        new Sound(3, "Rain Sound", "Peaceful rain for sleep", "/audio/rain.mp3", "/images/rain.jpg")
    );

    @GetMapping("/start")
    public String showSounds(Model model) {
        model.addAttribute("sounds", sounds);
        return "start";  // Renderiza start.html
    }
}