package es.swapsounds.service;

import es.swapsounds.model.Category;
import es.swapsounds.repository.CategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void addCategoryIfNotExists(String name) {
        String normalizedName = name.trim();
        if (!categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            Category category = new Category(normalizedName);
            categoryRepository.save(category);
        }
    }

    @Transactional
    public Category findOrCreateCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name.trim())
            .orElseGet(() -> {
                Category newCategory = new Category(name.trim());
                return categoryRepository.save(newCategory);
            });
    }

    @Transactional
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
} 
