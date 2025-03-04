package es.swapsounds.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class GreetingController {

    @GetMapping("/")
    public String landingPage(Model model) {
        return "landing-page";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        return "contact";
    }
    
}