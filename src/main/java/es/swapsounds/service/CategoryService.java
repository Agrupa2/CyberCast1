package es.swapsounds.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
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
        return categoryRepository.save(category);
    }

    // Update category
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category existingCategory = findCategoryById(id);
        
        // Actualizar campos necesarios
        existingCategory.setName(categoryDetails.getName());
        // Añadir más campos si es necesario
        
        return categoryRepository.save(existingCategory);
    }

    // Delete category
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    // Clase para manejar excepciones (puede estar en archivo aparte)
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}