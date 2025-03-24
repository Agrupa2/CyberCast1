package es.swapsounds.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.swapsounds.model.Category;
import es.swapsounds.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Obtain all categories
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    // Search category by id
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    // Create new category
    @Transactional
    public Category createCategory(Category category) {
        // Podrías añadir validaciones aquí (ej: nombre único)
    if (categoryRepository.existsByName(category.getName())) {
        throw new IllegalArgumentException("Category name already exists");
    }
        return categoryRepository.save(category);
    }

    // Update category, if a user wants to change the name of the category
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category existingCategory = findCategoryById(id);
        
        existingCategory.setName(categoryDetails.getName());
        
        return categoryRepository.save(existingCategory);
    }

    // Delete category
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

   // (Ejemplo simplificado en CategoryService)
    public Set<Category> processCategories(Set<String> categoryNames) {
        Set<Category> categories = new HashSet<>();
    
        for (String name : categoryNames) {
            Category category = categoryRepository.findByName(name)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no existe: " + name)); // if the user didn´t input a category that exist, it won´t be added to the sound
            categories.add(category);
        }
    
        return categories;
    }

    // Clase para manejar excepciones (puede estar en archivo aparte)
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}