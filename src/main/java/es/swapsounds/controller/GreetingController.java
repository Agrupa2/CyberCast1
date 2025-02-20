package es.swapsounds.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GreetingController {
    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("message", "Bienvenidos a SwapSounds");
        return "greeting_template";
    }

    @GetMapping("/hola")
    public String hola(Model model) {
        return "greeting_template";
    }

    @GetMapping("/")
    public String landingPage(Model model) {
        return "landing-page";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        return "contact";
    }

    @GetMapping("/comment_section")
    public String comment_section(Model model) {
        return "comment_section";
    }
}