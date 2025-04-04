package es.swapsounds.service;

import es.swapsounds.model.Category;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoryService {

    private Set<Category> categories = new HashSet<>();

    @PostConstruct
    private void initializeDefaultCategories() {
        addCategoryIfNotExists("Música");
        addCategoryIfNotExists("Podcast");
        addCategoryIfNotExists("Efectos de sonido");
        addCategoryIfNotExists("Naturaleza");
        addCategoryIfNotExists("Tecnología");
    }

    public void addCategoryIfNotExists(String name) {
        String normalizedName = name.trim().toLowerCase();
        boolean exists = categories.stream()
            .anyMatch(c -> c.getName().trim().equalsIgnoreCase(normalizedName));
        
        if (!exists) {
            categories.add(new Category(name.trim()));
        }
    }

    public Category findOrCreateCategory(String name) {
        return categories.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> {
                Category newCategory = new Category(name);
                categories.add(newCategory);
                return newCategory;
            });
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }
}
