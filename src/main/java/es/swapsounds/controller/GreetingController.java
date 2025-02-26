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

    @GetMapping("/comment_section")
    public String comment_section(Model model) {
        return "comment_section";
    }

}