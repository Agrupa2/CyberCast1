package es.swapsounds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import es.swapsounds.repository.CategoryRepository;

@Controller
public class CategoryController {
    
    @Autowired
    private CategoryRepository categories;
    @Autowired
    private SoundController sounds;
}
